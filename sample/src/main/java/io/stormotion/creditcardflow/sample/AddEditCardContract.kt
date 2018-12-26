package io.stormotion.creditcardflow.sample

import io.stormotion.creditcardflow.sample.mvp.BasePresenter
import io.stormotion.creditcardflow.sample.mvp.BaseView

interface AddEditCardContract {
    interface Presenter : BasePresenter {
        fun saveCreditCard(number: String,
                           holderName: String,
                           expiryDate: String,
                           cvv: String)
    }

    interface View : BaseView<Presenter> {
        fun showCreditCardSavedSuccessfully()
    }
}