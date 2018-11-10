package io.stormotion.creditcardflow.credit_card_flow

import java.io.Serializable

open class CreditCard(open var uuid: String? = null,
                      open var number: String? = null,
                      open var holderName: String? = null,
                      open var company: String? = null,
                      open var cvc: String? = null,
                      open var expiryDate: String? = null,
                      open var type: String? = null,
                      open var isPrimary: Boolean? = null,
                      open var companyIdentifier: String? = null) : Serializable