package io.stormotion.creditcardflow.sample

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.*
import io.stormotion.creditcardflow.CardFlowState
import io.stormotion.creditcardflow.CreditCardFlow
import io.stormotion.creditcardflow.CreditCardFlowListener
import io.stormotion.creditcardflow.sample.mvp.BaseFragment
import org.jetbrains.anko.longToast


class AddEditCardFragment : BaseFragment<AddEditCardContract.Presenter>(), AddEditCardContract.View {

    companion object {
        fun getInstance(card: CreditCard?) =
                AddEditCardFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ADD_EDIT_CARD_CREDIT_CARD_EXTRA, card)
                    }
                }

        const val ADD_EDIT_CARD_CREDIT_CARD_EXTRA = "add_edit_card_credit_card_extra"
    }

    private lateinit var mNextMenuItem: MenuItem
    private lateinit var mCreditCardFlow: CreditCardFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity) //crash early to make sure that an activity which initialized is AppCompatActivity and we can safely call it's action bar after that
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        mNextMenuItem = menu.findItem(R.id.ic_next)
        mNextMenuItem.actionView.setOnClickListener {
            when (mCreditCardFlow.currentState()) {
                CardFlowState.ACTIVE_CARD_NUMBER -> mCreditCardFlow.validateCreditCardNumber()
                CardFlowState.EXPIRATION -> mCreditCardFlow.validateCreditCardExpiryDate()
                CardFlowState.HOLDER -> mCreditCardFlow.validateCreditCardHolder()
                CardFlowState.CVV -> mCreditCardFlow.validateCreditCardCVV()
                else -> {
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed(activity!!)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_edit_credit_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCreditCardFlow = getView()!!.findViewById<CreditCardFlow>(R.id.credit_card_flow).apply {
            Handler().post({
                setCreditCard((arguments!!.getSerializable(ADD_EDIT_CARD_CREDIT_CARD_EXTRA) as CreditCard?)?.let {
                    io.stormotion.creditcardflow.CreditCard(it.number, it.holderName, it.cvc, it.expiryDate)
                })
                setCreditCardFlowListener(object : CreditCardFlowListener {
                    override fun onActiveCardNumberBeforeChangeToNext() {
                    }

                    override fun onCardExpiryDateBeforeChangeToNext() {
                    }

                    override fun onCardHolderBeforeChangeToNext() {
                    }

                    override fun onFromActiveToInactiveAnimationStart() {
                        mNextMenuItem.isVisible = false
                    }

                    override fun onFromInactiveToActiveAnimationStart() {
                        mNextMenuItem.isVisible = true
                    }

                    override fun onCardCvvBeforeChangeToNext() {
                    }

                    override fun onActiveCardNumberBeforeChangeToPrevious() {
                        activity!!.finish()
                    }

                    override fun onInactiveCardNumberBeforeChangeToNext() {
                    }

                    override fun onInactiveCardNumberBeforeChangeToPrevious() {
                        activity!!.finish()
                    }

                    override fun onCardExpiryDateBeforeChangeToPrevious() {
                    }

                    override fun onCardHolderBeforeChangeToPrevious() {
                    }

                    override fun onCardCvvBeforeChangeToPrevious() {
                    }

                    override fun onCardNumberValidatedSuccessfully(cardNumber: String) {
                    }

                    override fun onCardNumberValidationFailed(cardNumber: String) {
                    }

                    override fun onCardHolderValidatedSuccessfully(cardHolder: String) {
                    }

                    override fun onCardHolderValidationFailed(cardholder: String) {
                    }

                    override fun onCardExpiryDateValidatedSuccessfully(expiryDate: String) {
                    }

                    override fun onCardExpiryDateValidationFailed(expiryDate: String) {
                    }

                    override fun onCardExpiryDateInThePast(expiryDate: String) {
                    }

                    override fun onCardCvvValidatedSuccessfully(cvv: String) {
                    }

                    override fun onCardCvvValidationFailed(cvv: String) {
                    }

                    override fun onCreditCardFlowFinished(creditCard: io.stormotion.creditcardflow.CreditCard) {
                        mNextMenuItem.isVisible = false
                        activity!!.closeSoftKeyboard()
                        save()
                        context.longToast(resources.getString(R.string.credit_card_successfully_saved, creditCard))
                    }
                })
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        arguments!!.putSerializable(ADD_EDIT_CARD_CREDIT_CARD_EXTRA,
                CreditCard(mCreditCardFlow.creditCardNumber(),
                        mCreditCardFlow.creditCardHolder(),
                        mCreditCardFlow.creditCardCvvCode(),
                        mCreditCardFlow.creditCardExpiryDate()
                ))
    }

    override fun setPresenter(presenter: AddEditCardContract.Presenter) {
        mPresenter = presenter
    }

    override fun showCreditCardSavedSuccessfully() {
        activity!!.finish()
    }

    fun onBackPressed(activity: Activity) {
        when {
            mCreditCardFlow.currentState() != CardFlowState.ACTIVE_CARD_NUMBER -> mCreditCardFlow.previousState()
            else -> activity.finish()
        }
    }

    private fun save() {
        mPresenter.saveCreditCard(number = mCreditCardFlow.creditCardNumber(),
                holderName = mCreditCardFlow.creditCardHolder(),
                expiryDate = mCreditCardFlow.creditCardExpiryDate(),
                cvv = mCreditCardFlow.creditCardCvvCode())
    }
}