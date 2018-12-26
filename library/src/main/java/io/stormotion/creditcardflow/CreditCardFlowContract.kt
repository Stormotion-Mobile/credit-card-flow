package io.stormotion.creditcardflow

import io.stormotion.creditcardflow.mvp.BasePresenter
import io.stormotion.creditcardflow.mvp.BaseView

interface CreditCardFlowContract {
    interface Presenter : BasePresenter {
        fun getCreditCardLogo(creditCardNumber: String)
        fun checkIfShouldShowActiveFrontImmediately(state: CardFlowState, creditCardNumber: String?)
        fun validateCreditCardNumber(creditCardNumber: String)
        fun validateCreditCardHolder(creditCardHolder: String)
        fun validateCreditCardExpiryDate(creditExpiryDate: String)
        fun validateCreditCardCVV(creditCVV: String)
        fun checkCurrentCardPosition(state: CardFlowState, creditCard: CreditCard)
    }

    interface View : BaseView<Presenter> {
        fun showCreditCardLogo(creditCardEnum: CreditCardEnum)
        fun showNoCreditCardLogo()

        fun showCreditCardNumberValidatedSuccessfully()
        fun showCreditCardNumberFailedToValidate()

        fun showCreditCardHolderValidatedSuccessfully()
        fun showCreditCardHolderFailedToValidate()

        fun showCreditCardExpiryDateValidatedSuccessfully()
        fun showCreditCardExpiryDateFailedToValidate()
        fun showCreditCardExpiryDateIsAlreadyExpired()

        fun showCreditCardCvvValidatedSuccessfully()
        fun showCreditCardCvvFailedToValidate()

        fun showCreditCardActiveFront(creditCardEnum: CreditCardEnum)
        fun showCreditCardActiveBottom(creditCardEnum: CreditCardEnum)
    }
}