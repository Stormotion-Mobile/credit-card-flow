package io.stormotion.creditcardflow

import java.io.Serializable

data class CreditCard(val number: String? = null,
                 val holderName: String? = null,
                 val cvc: String? = null,
                 val expiryDate: String? = null) : Serializable