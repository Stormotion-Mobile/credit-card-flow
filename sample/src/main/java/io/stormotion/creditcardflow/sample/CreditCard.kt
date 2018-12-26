package io.stormotion.creditcardflow.sample

import java.io.Serializable

open class CreditCard(open var number: String? = null,
                      open var holderName: String? = null,
                      open var cvc: String? = null,
                      open var expiryDate: String? = null
) : Serializable
