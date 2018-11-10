package io.stormotion.creditcardflow.credit_card_flow

interface CreditCardFlowListener {
    fun onCardNumberBeforeChangeToNext()
    fun onCardExpiryDateBeforeChangeToNext()
    fun onCardHolderBeforeChangeToNext()
    fun onCardCvvBeforeChangeToNext()

    fun onCardNumberBeforeChangeToPrevious()
    fun onCardExpiryDateBeforeChangeToPrevious()
    fun onCardHolderBeforeChangeToPrevious()
    fun onCardCvvBeforeChangeToPrevious()

    fun onCardNumberValidatedSuccessfully(cardNumber: String)
    fun onCardNumberValidationFailed(cardNumber: String)
    fun onCardHolderValidatedSuccessfully(cardHolder: String)
    fun onCardHolderValidationFailed(cardholder: String)
    fun onCardExpiryDateValidatedSuccessfully(expiryDate: String)
    fun onCardExpiryDateValidationFailed(expiryDate: String)
    fun onCardExpiryDateInThePast(expiryDate: String)
    fun onCardCvvValidatedSuccessfully(cvv: String)
    fun onCardCvvValidationFailed(cvv: String)

    fun onFromActiveToInactiveAnimationStart()
    fun onFromInactiveToActiveAnimationStart()
}