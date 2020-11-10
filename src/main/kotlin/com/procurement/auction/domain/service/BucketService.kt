package com.procurement.auction.domain.service

import com.procurement.auction.configuration.properties.DefaultSchedulerProperties
import com.procurement.auction.configuration.properties.SchedulerProperties
import com.procurement.auction.domain.logger.Logger
import com.procurement.auction.domain.logger.debug
import com.procurement.auction.domain.logger.warn
import com.procurement.auction.domain.model.Cpid
import com.procurement.auction.domain.model.auction.EstimatedDurationAuction
import com.procurement.auction.domain.model.bucket.AuctionsTimes
import com.procurement.auction.domain.model.bucket.Bucket
import com.procurement.auction.domain.model.bucket.id.BucketId
import com.procurement.auction.domain.model.country.Country
import com.procurement.auction.domain.model.slots.id.SlotId
import com.procurement.auction.domain.model.version.RowVersion
import com.procurement.auction.domain.repository.BucketRepository
import com.procurement.auction.domain.repository.CalendarRepository
import com.procurement.auction.exception.BucketForReleaseNotFound
import com.procurement.auction.exception.BucketIsAlreadyExistException
import com.procurement.auction.exception.app.CalendarNoDataException
import com.procurement.auction.exception.app.OutOfAuctionsException
import com.procurement.auction.exception.database.OptimisticLockException
import com.procurement.auction.infrastructure.logger.Slf4jLogger
import org.springframework.stereotype.Service
import java.time.LocalDate

interface BucketService {
    fun create(id: BucketId): Bucket

    fun booking(cpid: Cpid,
                country: Country,
                dateStartAuction: LocalDate,
                estimates: List<EstimatedDurationAuction>): AuctionsTimes

    fun release(cpid: Cpid, id: BucketId, slotsIds: Set<SlotId>)
}

@Service
class BucketServiceImpl(
    private val bucketRepository: BucketRepository,
    private val calendarRepository: CalendarRepository,
    private val allocationStrategy: AllocationStrategy,
    schedulerProperties: SchedulerProperties
) : BucketService {
    companion object {
        private val log: Logger = Slf4jLogger()
    }

    private val defaultSchedulerProperties: DefaultSchedulerProperties = DefaultSchedulerProperties(schedulerProperties)

    override fun create(id: BucketId): Bucket {
        return Bucket(
            id = id,
            rowVersion = RowVersion.of(),
            allocationStrategy = allocationStrategy,
            slots = defaultSchedulerProperties.defaultSlots.associateBy { it.id }
        ).also {
            log.debug { "Created $it" }
        }
    }

    override fun booking(cpid: Cpid,
                         country: Country,
                         dateStartAuction: LocalDate,
                         estimates: List<EstimatedDurationAuction>): AuctionsTimes {

        var date = dateStartAuction

        TryOnDate@
        while (true) {
            val selectedDate = getWorkDayByCalendar(country, date)

            OptimisticLock@
            while (true) {
                val bucketId = BucketId(selectedDate, country)
                val bucket = bucketRepository.load(bucketId)
                    ?: create(BucketId(selectedDate, country))

                val auctionTimes = bucket.booking(cpid, estimates)
                    ?: if (bucket.isNew) {
                        log.debug { "Could not book in new bucket ($date)." }
                        throw OutOfAuctionsException()
                    } else {
                        date = date.plusDays(1)
                        log.debug { "Attempting to use a bucket on another date: '$date'." }
                        continue@TryOnDate
                    }

                try {
                    bucketRepository.save(bucket)
                    log.debug { "Booked slots: '${auctionTimes.slotsIds}' in the bucket: '${bucket.id}'." }
                    return auctionTimes
                } catch (exception: Exception) {
                    when (exception) {
                        is OptimisticLockException,
                        is BucketIsAlreadyExistException -> {
                            log.warn { "Unsuccessful attempt to saving in database data about booked slots: '${auctionTimes.slotsIds}' in the bucket: '${bucket.id}'. ${exception.message}" }
                            continue@OptimisticLock
                        }
                        else -> throw exception
                    }
                }
            }
        }
    }

    override fun release(cpid: Cpid, id: BucketId, slotsIds: Set<SlotId>) {
        OptimisticLock@
        while (true) {
            val bucket = bucketRepository.load(id)
                ?: throw BucketForReleaseNotFound()

            val hasChanged = bucket.release(cpid, slotsIds)

            while (hasChanged) {
                try {
                    bucketRepository.save(bucket)
                    log.debug { "Release slots: '$slotsIds' in the bucket: '$id'." }
                    return
                } catch (exception: OptimisticLockException) {
                    log.warn { "Unsuccessful attempt to saving in database data about release slots: '$slotsIds' in the bucket: '$id'. ${exception.message}" }
                    continue@OptimisticLock
                }
            }
        }
    }

    private tailrec fun getWorkDayByCalendar(country: Country, dateStartAuction: LocalDate): LocalDate {
        log.debug { "Get WorkDay from calendar (country: '$country', date: '$dateStartAuction')" }
        val year = dateStartAuction.year
        val month = dateStartAuction.month.value
        val days = calendarRepository.loadWorkDays(country, year, month)
        if (days.isEmpty())
            throw CalendarNoDataException(country = country,
                                          year = year,
                                          month = month)

        val numberDay = dateStartAuction.dayOfMonth
        return if (days.contains(numberDay)) {
            log.debug { "Return WorkDay from calendar (country: '$country', date: '$dateStartAuction'): '$dateStartAuction'" }
            dateStartAuction
        } else
            getWorkDayByCalendar(country, dateStartAuction.plusDays(1))
    }
}
