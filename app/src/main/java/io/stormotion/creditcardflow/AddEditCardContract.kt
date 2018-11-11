package io.stormotion.creditcardflow

import io.stormotion.creditcardflow.credit_card_flow.CreditCardEnum
import io.stormotion.creditcardflow.mvp.BasePresenter
import io.stormotion.creditcardflow.mvp.BaseView

interface AddEditCardContract {
    interface Presenter : BasePresenter {
        fun validateCreditCardTypeAndPriority(creditCardType: String, creditCardPriority: String)
        fun saveCreditCard(number: String,
                           holderName: String,
                           expiryDate: String,
                           cvv: String,
                           type: CardType,
                           isPrimary: Boolean)
    }

    interface View: BaseView<Presenter> {
        fun showCreditCardPriorityAndTypeValidatedSuccessfully()
        fun showCreditCardPriorityIsEmpty()
        fun showCreditCardTypeIsEmpty()

        fun showCreditCardSavedSuccessfully()
        fun showCreditCardFailedToSave(resourceId: Int)

    }

    enum class CardType {
        PERSONAL, BUSINESS
    }
}