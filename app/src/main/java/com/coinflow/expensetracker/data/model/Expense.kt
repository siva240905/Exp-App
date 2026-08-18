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
    val paymentMethod: String = "UPI"
)
