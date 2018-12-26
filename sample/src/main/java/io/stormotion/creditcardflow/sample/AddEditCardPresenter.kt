package io.stormotion.creditcardflow.sample

class AddEditCardPresenter(val view: AddEditCardContract.View) : AddEditCardContract.Presenter {
    init {
        view.setPresenter(this)
    }

    override fun subscribe() {
    }

    override fun saveCreditCard(number: String,
                                holderName: String,
                                expiryDate: String,
                                cvv: String) {
        view.showCreditCardSavedSuccessfully()
    }
}