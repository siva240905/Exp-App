package com.coinflow.expensetracker.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ExpenseContainer(
    @SerializedName("expenses")
    val expenses: List<Expense> = emptyList()
)

data class Expense(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("amount")
    val amount: Double = 0.0,
    
    @SerializedName("category")
    val category: String = "Food",
    
    @SerializedName("description")
    val description: String = "",
    
    @SerializedName("date")
    val date: String = "",
    
    @SerializedName("paymentMethod")
    val paymentMethod: String = "UPI",

    @SerializedName("type")
    val type: String = TYPE_SEND
) {
    companion object {
        const val TYPE_SEND = "SEND"
        const val TYPE_RECEIVE = "RECEIVE"
    }
}
