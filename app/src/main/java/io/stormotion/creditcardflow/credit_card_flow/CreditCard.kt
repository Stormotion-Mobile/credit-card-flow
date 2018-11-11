package io.stormotion.creditcardflow.credit_card_flow

import java.io.Serializable

class CreditCard(val number: String? = null,
                 val holderName: String? = null,
                 val cvc: String? = null,
                 val expiryDate: String? = null) : Serializable