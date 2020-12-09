package com.procurement.auction.domain.service

import com.procurement.auction.configuration.properties.AuctionProperties
import com.procurement.auction.domain.model.bid.id.BidId
import com.procurement.auction.domain.model.lotId.LotId
import com.procurement.auction.domain.model.ocid.Ocid
import com.procurement.auction.domain.model.sign.Sign
import org.springframework.stereotype.Service
import java.net.URL

interface UrlGeneratorService {
    fun forModality(ocid: Ocid, relatedLot: LotId): String
    fun forBid(ocid: Ocid, relatedLot: LotId, bidId: BidId, sign: Sign): String
}

@Service
class UrlGeneratorServiceImpl(
    private val auctionProperties: AuctionProperties
) : UrlGeneratorService {

    private val urlAuction = genUrlAuctions()

    override fun forModality(ocid: Ocid, relatedLot: LotId): String =
        "$urlAuction/auctions/${ocid}/${relatedLot.value}"

    override fun forBid(ocid: Ocid, relatedLot: LotId, bidId: BidId, sign: Sign): String =
        "${forModality(ocid, relatedLot)}?bid_id=${bidId.value}&sign=${sign.value}"

    private fun genUrlAuctions(): String {
        val url = auctionProperties.url
            ?: throw IllegalStateException("Not set the url of an tender.")
        val protocol = url.protocol
            ?: throw IllegalStateException("Not set the scheme of the url.")
        val host = url.host
            ?: throw IllegalStateException("Not set the domain name of the url.")
        return getUrlASCII("$protocol://$host")
    }

    private fun getUrlASCII(uri: String): String = try {
        URL(uri).toURI().toASCIIString()
    } catch (exception: Exception) {
        throw IllegalStateException("Invalid the tender url: '$uri'.")
    }
}