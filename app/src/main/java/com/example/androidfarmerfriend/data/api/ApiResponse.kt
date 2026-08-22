package com.example.androidfarmerfriend.data.api

// --- VegetableMarketPrice.com API DTOs ---

data class VegetableMarketResponse<T>(
    val startDateLimit: String?,
    val date: String?,
    val dataHeaders: List<DataHeader>?,
    val data: List<T>?
)

data class DataHeader(
    val headerName: String?,
    val headerType: String?,
    val headerValue: String?,
    val orderId: Int?
)

data class VegetableItem(
    val columnNameEng: String?,
    val vegetablename: String?,
    val price: Any?,
    val retailprice: Any?,
    val units: String?,
    val id: String?
)

data class FruitItem(
    val columnNameEng: String?,
    val fruitname: String?,
    val price: Any?,
    val retailprice: Any?,
    val units: String?,
    val id: String?
)

data class NonVegItem(
    val columnNameEng: String?,
    val nonvegname: String?,
    val price: Any?,
    val units: String?,
    val id: String?
)

data class GoldItem(
    val columnNameEng: String?,
    val name: String?,
    val price: Any?,
    val units: String?,
    val id: String?
)

// --- NECC Egg Price API DTOs ---

data class NcecEggPriceItem(
    val city: String?,
    val price: Any?,
    val avg: Any?
)
