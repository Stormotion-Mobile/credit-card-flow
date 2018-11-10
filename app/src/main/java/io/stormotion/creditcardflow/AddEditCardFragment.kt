package io.stormotion.creditcardflow

import android.app.Activity
import android.app.AlertDialog
import android.app.FragmentManager
import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import io.stormotion.creditcardflow.credit_card_flow.*
import io.stormotion.creditcardflow.mvp.BaseFragment
import kotlinx.android.synthetic.main.add_edit_credit_card_fragment.*
import kotlinx.android.synthetic.main.credit_card_flow.*
import kotlinx.android.synthetic.main.credit_card_successfully_added.*
import kotlinx.android.synthetic.main.credit_card_type_and_priority.*
import org.jetbrains.anko.toast


class AddEditCardFragment : BaseFragment<AddEditCardContract.Presenter>(), AddEditCardContract.View {

    companion object {
        fun getInstance(card: CreditCard?) =
                AddEditCardFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ADD_EDIT_CARD_CREDIT_CARD_EXTRA, card)
                    }
                }

        const val ADD_EDIT_CARD_CREDIT_CARD_EXTRA = "add_edit_card_credit_card_extra"

        object PositionInViewFlipper {
            val CREDIT_CARD_NUMBER_HOLDER_CVV = 0
            val CREDIT_CARD_TYPE_PRIORITY = 1
            val CREDIT_CARD_SUCCESS = 2
        }
    }

    private var card: CreditCard? = null
    private lateinit var mNextMenuItem: MenuItem
    private lateinit var mCreditCardFlow: CreditCardFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        card = (arguments!!.getSerializable(ADD_EDIT_CARD_CREDIT_CARD_EXTRA) as CreditCard?)?.let { it }
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
                CardFlowState.CARD_NUMBER -> mCreditCardFlow.validateCreditCardNumber()
                CardFlowState.EXPIRATION -> mCreditCardFlow.validateCreditCardExpiryDate()
                CardFlowState.HOLDER -> mCreditCardFlow.validateCreditCardHolder()
                CardFlowState.CVV -> mCreditCardFlow.validateCreditCardCVV()
                null -> {
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

        mCreditCardFlow = getView()!!.findViewById(R.id.credit_card_flow)
        mCreditCardFlow.setCreditCardFlowListener(object : CreditCardFlowListener {
            override fun onCardNumberBeforeChangeToNext() {
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
                mNextMenuItem.isVisible = false
                add_edit_card_steps_view_flipper.displayedChild = AddEditCardFragment.Companion.PositionInViewFlipper.CREDIT_CARD_TYPE_PRIORITY
                activity!!.closeSoftKeyboard()
            }


            override fun onCardNumberBeforeChangeToPrevious() {
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
        })

        configureTypeAndPriorityViews(activity!!.fragmentManager)
    }

    override fun setPresenter(presenter: AddEditCardContract.Presenter) {
        mPresenter = presenter
    }

    override fun showCreditCardPriorityAndTypeValidatedSuccessfully() {
        save()
    }

    override fun showCreditCardPriorityIsEmpty() {
        context!!.toast(R.string.credit_card_priority_is_empty)
    }

    override fun showCreditCardTypeIsEmpty() {
        context!!.toast(R.string.credit_card_type_is_empty)
    }

    override fun showCreditCardSavedSuccessfully() {
        add_edit_card_steps_view_flipper.displayedChild = PositionInViewFlipper.CREDIT_CARD_SUCCESS
    }

    override fun showCreditCardFailedToSave(resourceId: Int) {
        save_card_container.showNext()
        context!!.toast(getString(resourceId))
    }

    fun onBackPressed(activity: Activity) {
        when {
            add_edit_card_steps_view_flipper.displayedChild == PositionInViewFlipper.CREDIT_CARD_TYPE_PRIORITY -> {
                mNextMenuItem.isVisible = true
                add_edit_card_steps_view_flipper.displayedChild = AddEditCardFragment.Companion.PositionInViewFlipper.CREDIT_CARD_NUMBER_HOLDER_CVV
                input_edit_cvv_code.requestFocus() //TODO getter?
                showKeyboard(activity, input_edit_cvv_code)
            }
            add_edit_card_steps_view_flipper.displayedChild == PositionInViewFlipper.CREDIT_CARD_SUCCESS -> activity.finish()
            mCreditCardFlow.currentState() != CardFlowState.CARD_NUMBER -> mCreditCardFlow.previousState()
            else -> handleBackPressed(activity)
        }

    }

    private fun handleBackPressed(activity: Activity) {
        if (hasUserAddedCreditCardDetails()) {
            confirmGoBack(activity)
        } else {
            activity.finish()
        }
    }

    private fun save() {
        card = card?.let { it } ?: CreditCard()
        with(card!!) {
            number = mCreditCardFlow.creditCardNumber()
            expiryDate = mCreditCardFlow.creditCardExpiryDate()
            holderName = mCreditCardFlow.creditCardHolder()
            cvc = mCreditCardFlow.creditCardCvvCode()
        }
        val creditCardType = credit_card_type.text.toString()
        val creditCardPriority = credit_card_priority.text.toString()

        save_card_container.showNext()
        mPresenter.saveCreditCard(number = mCreditCardFlow.creditCardNumber(),
                holderName = mCreditCardFlow.creditCardHolder(),
                expiryDate = mCreditCardFlow.creditCardExpiryDate(),
                cvv = mCreditCardFlow.creditCardCvvCode(),
                isAirplus = card?.let {
                    it.type == CreditCardEnum.AIRPLUS.naming
                } ?: false,
                type = when (creditCardType) {
                    resources.getString(AddEditCardFragment.CardType.PERSONAL.textRes) -> AddEditCardContract.CardType.PERSONAL
                    resources.getString(AddEditCardFragment.CardType.BUSINESS.textRes) -> AddEditCardContract.CardType.BUSINESS
                    else -> throw Exception("Not valid type of card = $creditCardType")
                },
                isPrimary = when (creditCardPriority) {
                    resources.getString(CardPriority.PRIMARY.textRes) -> true
                    resources.getString(CardPriority.SECONDARY.textRes) -> false
                    else -> throw Exception("Not valid priority of card = $creditCardPriority")
                }
        )
    }

    /**
     * Create a dialog to confirm losing current entered Credit Card details
     */
    private fun confirmGoBack(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.dialog_confirmation)
        builder.setMessage(R.string.dialog_card_confirmation_text)
        builder.setPositiveButton(R.string.yes) { dialogInterface, which ->
            activity.finish()
        }
        builder.setNegativeButton(R.string.no) { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun hasUserAddedCreditCardDetails(): Boolean {
        val number = mCreditCardFlow.creditCardNumber()
        val holderName = mCreditCardFlow.creditCardHolder()
        val expiryDate = mCreditCardFlow.creditCardExpiryDate()
        val cvv = mCreditCardFlow.creditCardCvvCode()
        return !TextUtils.isEmpty(number)
                || !TextUtils.isEmpty(holderName)
                || !TextUtils.isEmpty(expiryDate)
                || !TextUtils.isEmpty(cvv)
    }

    private fun configureTypeAndPriorityViews(fragmentManager: FragmentManager) {
        credit_card_priority.setOnClickListener {
            val priorityPrimary = resources.getString(R.string.credit_card_priority_primary)
            val prioritySecondary = resources.getString(R.string.credit_card_priority_secondary)
            val pickerItems = arrayListOf(ItemPickerDialogFragment.Item(priorityPrimary, priorityPrimary),
                    ItemPickerDialogFragment.Item(prioritySecondary, prioritySecondary)
            )

            val dialog = ItemPickerDialogFragment.newInstance(
                    resources.getString(R.string.credit_card_set_priority_title),
                    pickerItems,
                    pickerItems.indexOfFirst { it.stringValue == credit_card_priority.text.toString() }
            ).setListener { _, item, _ ->
                credit_card_priority.setText(item.stringValue)
            }
            dialog.show(fragmentManager, "CreditCardPriority")
        }
        credit_card_type.setOnClickListener {
            val typePersonal = resources.getString(CardType.PERSONAL.textRes)
            val typeBusiness = resources.getString(CardType.BUSINESS.textRes)
            val pickerItems = arrayListOf(ItemPickerDialogFragment.Item(typePersonal, typePersonal),
                    ItemPickerDialogFragment.Item(typeBusiness, typeBusiness)
            )

            val dialog = ItemPickerDialogFragment.newInstance(
                    resources.getString(R.string.credit_card_set_type_title),
                    pickerItems,
                    pickerItems.indexOfFirst { it.stringValue == credit_card_type.text.toString() }
            ).setListener { _, item, _ ->
                credit_card_type.setText(item.stringValue)
            }

            dialog.show(fragmentManager, "CreditCardTitle")

        }
        save_card.setOnClickListener {
            mPresenter.validateCreditCardTypeAndPriority(creditCardType = credit_card_type.text.toString(),
                    creditCardPriority = credit_card_priority.text.toString()
            )
        }
        back_to_settings_btn.setOnClickListener {
            activity!!.finish()
        }
    }

    private fun showKeyboard(activity: Activity, view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    private enum class CardType(@StringRes val textRes: Int) {
        PERSONAL(R.string.credit_card_type_personal), BUSINESS(R.string.credit_card_type_business)
    }

    private enum class CardPriority(@StringRes val textRes: Int) {
        PRIMARY(R.string.credit_card_priority_primary), SECONDARY(R.string.credit_card_priority_secondary)
    }

}