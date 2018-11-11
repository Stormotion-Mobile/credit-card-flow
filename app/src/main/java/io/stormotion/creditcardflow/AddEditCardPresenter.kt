package io.stormotion.creditcardflow

class AddEditCardPresenter(val view: AddEditCardContract.View) : AddEditCardContract.Presenter {
    init {
        view.setPresenter(this)
    }

    override fun subscribe() {
    }

    override fun validateCreditCardTypeAndPriority(creditCardType: String, creditCardPriority: String) {
        val creditCardTypeValid = creditCardType.isNotEmpty()
        val creditCardPriorityValid = creditCardPriority.isNotEmpty()
        if (!creditCardPriorityValid) {
            view.showCreditCardPriorityIsEmpty()
        }
        if (!creditCardTypeValid) {
            view.showCreditCardTypeIsEmpty()
        }
        if (creditCardPriorityValid && creditCardTypeValid) {
            view.showCreditCardPriorityAndTypeValidatedSuccessfully()
        }
    }

    override fun saveCreditCard(number: String,
                                holderName: String,
                                expiryDate: String,
                                cvv: String,
                                type: AddEditCardContract.CardType,
                                isPrimary: Boolean) {
        view.showCreditCardSavedSuccessfully()
    }
}