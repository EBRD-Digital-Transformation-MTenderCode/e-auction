package com.procurement.auction.converter

inline fun <reified S, reified T> AuctionConversionService.convert(source: S): T =
    convert(S::class.java, T::class.java, source)

inline fun <reified T, reified V> AuctionConversionService.addConverter(converter: Converter<T, V>) =
    addConverter(T::class.java, V::class.java, converter)

interface Converter<S, T> {
    fun convert(source: S): T
}

interface AuctionConversionService {
    fun <S, T> convert(sourceType: Class<S>, targetType: Class<T>, source: S): T
    fun <S, T> addConverter(sourceType: Class<S>, targetType: Class<T>, converter: Converter<S, T>)
}

class AuctionConversionServiceImpl : AuctionConversionService {
    private val converters = mutableMapOf<Class<*>, MutableMap<Class<*>, Converter<*, *>>>()

    override fun <S, T> addConverter(sourceType: Class<S>, targetType: Class<T>, converter: Converter<S, T>) {
        val targets = converters[sourceType]
        if (targets == null) {
            converters[sourceType] = mutableMapOf<Class<*>, Converter<*, *>>().apply {
                this[targetType] = converter
            }
        } else {
            if (targets.containsKey(targetType))
                throw IllegalStateException("Converter from '${sourceType.canonicalName}' to '${targetType.canonicalName}' is already.")
            else
                targets[targetType] = converter
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S, T> convert(sourceType: Class<S>, targetType: Class<T>, source: S): T {
        val convertersBySource = converters[sourceType]
            ?: throw IllegalStateException("Converter from '${sourceType.canonicalName}' to '${targetType.canonicalName}' is not found.")

        val converter = convertersBySource[targetType] as? Converter<S, T>
            ?: throw IllegalStateException("Converter from '${sourceType.canonicalName}' to '${targetType.canonicalName}' is not found.")

        return converter.convert(source)
    }
}