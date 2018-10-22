package com.procurement.auction.exception

class OutOfAuctionException : RuntimeException("Unable to schedule auctions. Too many auctions.")