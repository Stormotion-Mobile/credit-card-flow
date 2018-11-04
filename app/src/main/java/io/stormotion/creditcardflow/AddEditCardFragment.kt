package io.stormotion.creditcardflow

import android.animation.*
import android.app.Activity
import android.app.AlertDialog
import android.app.FragmentManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.text.TextUtils
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import io.stormotion.creditcardflow.mvp.BaseFragment
import kotlinx.android.synthetic.main.add_edit_credit_card_fragment.*
import kotlinx.android.synthetic.main.credit_card_active_bottom_side.*
import kotlinx.android.synthetic.main.credit_card_active_front_side.*
import kotlinx.android.synthetic.main.credit_card_inactive.*
import kotlinx.android.synthetic.main.credit_card_successfully_added.*
import kotlinx.android.synthetic.main.credit_card_type_and_priority.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast
import java.lang.ref.WeakReference


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

        //spacing between 4 digits of credit number
        private const val SPACING_CREDIT_NUMBER = "   "
    }

    private var showingActiveFront = false //indicate if active front or inactive is shown
    private var showingBottomFront = false //indicate if active front or active bottom is shown
    private var inSet: AnimatorSet? = null
    private var outSet: AnimatorSet? = null
    private var stateMachine: CardFlowStateMachine = CardFlowStateMachine()
    private var card: CreditCard? = null
    private lateinit var mNextMenuItem: MenuItem

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
            when (stateMachine.currentState()) {
                CardFlowState.CARD_NUMBER -> validateCreditCardNumber()
                CardFlowState.EXPIRATION -> mPresenter.validateCreditCardExpiryDate(
                        input_edit_expired_date.text.toString()
                )
                CardFlowState.HOLDER -> mPresenter.validateCreditCardHolder(
                        input_edit_card_holder.text.toString()
                )
                AddEditCardFragment.CardFlowState.CVV -> mPresenter.validateCreditCardCVV(
                        input_edit_cvv_code.text.toString()
                )
                AddEditCardFragment.CardFlowState.TYPE_PRIORITY -> save()
                AddEditCardFragment.CardFlowState.SUCCESS, null -> {
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            if (stateMachine.currentState() != CardFlowState.CARD_NUMBER)
                stateMachine.previousState(activity!!)
            else handleBackPressed(activity!!)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    fun onBackPressed(activity: Activity) {
        if (stateMachine.currentState() != CardFlowState.CARD_NUMBER)
            stateMachine.previousState(activity)
        else handleBackPressed(activity)

    }

    private fun handleBackPressed(activity: Activity) {
        if (hasUserAddedCreditCardDetails()) {
            confirmGoBack(activity)
        } else {
            activity.finish()
        }
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_edit_credit_card_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        mPresenter.subscribe()
    }

    override fun onPause() {
        super.onPause()
        mPresenter.unsubscribe()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTextListeners()
        configureViewPager()
        setKeyboardListeners()

        input_edit_card_number.requestFocus()

        configureTypeAndPriorityViews(activity!!.fragmentManager)

        inSet = AnimatorInflater.loadAnimator(context, R.animator.card_flip_in) as AnimatorSet
        outSet = AnimatorInflater.loadAnimator(context, R.animator.card_flip_out) as AnimatorSet

        setViewsIfCardIsNotNull()
    }

    override fun setPresenter(presenter: AddEditCardContract.Presenter) {
        mPresenter = presenter
    }

    override fun showCreditCardLogo(creditCardEnum: CreditCardEnum) {
        val logoDrawable = ContextCompat.getDrawable(context!!, creditCardEnum.cardDrawable!!)
        val gradientDrawable = ContextCompat.getDrawable(context!!, creditCardEnum.gradientDrawable!!)
        flipBetweenActiveFrontAndInactiveAppearance(logoDrawable,
                gradientDrawable,
                toActive = true)
    }

    override fun showNoCreditCardLogo() {
        flipBetweenActiveFrontAndInactiveAppearance(null,
                null,
                toActive = false)
    }

    override fun showCreditCardNumberValidatedSuccessfully() {
        stateMachine.nextState(activity!!)
    }

    override fun showCreditCardNumberFailedToValidate() {
        context!!.toast(R.string.credit_card_number_is_not_valid)
    }

    override fun showCreditCardHolderValidatedSuccessfully() {
        stateMachine.nextState(activity!!)
    }

    override fun showCreditCardHolderFailedToValidate() {
        context!!.toast(R.string.credit_card_holder_is_not_valid)
    }

    override fun showCreditCardExpiryDateValidatedSuccessfully() {
        stateMachine.nextState(activity!!)
    }

    override fun showCreditCardExpiryDateFailedToValidate() {
        context!!.toast(R.string.credit_card_expiry_date_is_not_valid)
    }

    override fun showCreditCardExpiryDateIsAlreadyExpired() {
        context!!.toast(R.string.error_expiry_date_in_the_past)
    }

    override fun showCreditCardCvvValidatedSuccessfully() {
        stateMachine.nextState(activity!!)
    }

    override fun showCreditCardCvvFailedToValidate() {
        context!!.toast(R.string.credit_card_cvv_is_not_valid)
    }

    override fun showCreditCardSavedSuccessfully() {
        stateMachine.nextState(activity!!)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }

    override fun showCreditCardFailedToSave(resourceId: Int) {
        save_card_container.showNext()
        context!!.toast(getString(resourceId))
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

    private fun setTextListeners() {
        var cardNumberLock = false
        input_edit_card_number.afterTextChanged { s ->
            val noWhiteSpacesCardNumber = s.removeNotDigits().toString()

            val maxCreditCardNumberLength = 19 //based on https://www.freeformatter.com/credit-card-number-generator-validator.html (List of credit card number formats)
            if (!(cardNumberLock || noWhiteSpacesCardNumber.length > maxCreditCardNumberLength)) {
                cardNumberLock = true
                s.replace(0, s.length, prettyFormatCreditCardNumber(noWhiteSpacesCardNumber))
                mPresenter.getCreditCardLogo(noWhiteSpacesCardNumber)
                text_card_number.text = s.toString()
                label_card_number.text = s.toString().replace(SPACING_CREDIT_NUMBER, " ")
                cardNumberLock = false
            } else if (!cardNumberLock) {
                cardNumberLock = true
                val numberCharactersToRemove = noWhiteSpacesCardNumber.length - maxCreditCardNumberLength
                val savedCharacters = noWhiteSpacesCardNumber.removeRange(startIndex = noWhiteSpacesCardNumber.length - numberCharactersToRemove,
                        endIndex = noWhiteSpacesCardNumber.length)
                s.replace(0, s.length, prettyFormatCreditCardNumber(savedCharacters))
                mPresenter.getCreditCardLogo(noWhiteSpacesCardNumber)
                text_card_number.text = s.toString()
                label_card_number.text = s.toString().replace(SPACING_CREDIT_NUMBER, " ")
                cardNumberLock = false
            }
        }

        var cardHolderLock = false
        input_edit_card_holder.afterTextChanged { s ->
            if (!cardHolderLock) {
                cardHolderLock = true
                val onlyLettersAndSpacesCardHolder = s.filter { it.isLetter() || it.isWhitespace() }
                val onlySingleSpaceBetweenWords = onlyLettersAndSpacesCardHolder.trimStart().replace(Regex("\\s+"), " ")
                s.replace(0, s.length, onlySingleSpaceBetweenWords)
                text_card_holder.text = s.toString()
                cardHolderLock = false
            }
        }

        var expiredDate = ""
        input_edit_expired_date.afterTextChanged { s ->
            if (expiredDate.length < s.length) {
                if (s.length == 1) {
                    val month = s.toString().toIntOrNull()
                    if (month != null) {
                        if (month > 1) {
                            s.replace(0, s.length, "0$month/")
                        }
                    } else {
                        s.clear()
                    }
                } else if (s.length == 2) {
                    val month = s.toString().toIntOrNull()
                    if (month != null) {
                        s.append("/")
                    } else {
                        s.replace(0, s.length, "0${s.first()}/")
                    }
                } else if (s.length > 3 && s.last().equals('/')) {
                    s.replace(0, s.length, s.toString().removeSuffix("/"))
                }
            }
            expiredDate = s.toString()
            text_expired_date.text = expiredDate
        }

        input_edit_cvv_code.afterTextChanged { s ->
            text_cvc.text = s.toString()
            text_cvc.inputType = if (s.toString().isEmpty()) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            } else {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }

        }
    }

    private fun configureViewPager() {
        val adapter = MyPagerAdapter(WeakReference(activity!!))
        view_pager.adapter = adapter
        view_pager.setPagingEnabled(false)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        updateProgressBar(25)
                        input_edit_card_number.isFocusableInTouchMode = true
                        input_edit_expired_date.isFocusable = false
                        input_edit_card_holder.isFocusable = false
                        input_edit_cvv_code.isFocusable = false
                        input_edit_card_number.requestFocus()
                        return
                    }
                    1 -> {
                        updateProgressBar(50)
                        input_edit_card_number.isFocusable = false
                        input_edit_expired_date.isFocusableInTouchMode = true
                        input_edit_card_holder.isFocusable = false
                        input_edit_cvv_code.isFocusable = false
                        input_edit_expired_date.requestFocus()
                        return
                    }
                    2 -> {
                        updateProgressBar(75)
                        input_edit_card_number.isFocusable = false
                        input_edit_expired_date.isFocusable = false
                        input_edit_card_holder.isFocusableInTouchMode = true
                        input_edit_cvv_code.isFocusable = false
                        input_edit_card_holder.requestFocus()
                        return
                    }
                    3 -> {
                        updateProgressBar(100)
                        input_edit_card_number.isFocusable = false
                        input_edit_expired_date.isFocusable = false
                        input_edit_card_holder.isFocusable = false
                        input_edit_cvv_code.isFocusableInTouchMode = true
                        input_edit_cvv_code.requestFocus()
                        return
                    }
                    4 -> {
                        input_edit_card_number.isFocusable = false
                        input_edit_expired_date.isFocusable = false
                        input_edit_card_holder.isFocusable = false
                        input_edit_cvv_code.isFocusable = false
                        return
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * Helper method that takes into consideration this scenario: User input is 3 characters
     * starting with 1. Only in this case the auto prefix of 0 is not done automatically but only
     * before validating on click.
     * 1. Check if text has 4(3 chars input +"/") chars and it start with 1 eg. 11/2
     * 2. Removes "/" char and adds 0 prefix resulting in 0112
     * 3. Add "/" in the middle of the string resulting in 01/12
     * 4. Set the result to the input text
     */
    private fun optionallyFillExpirationDateMonth() {
        if (input_edit_expired_date.text.length == 4 &&
                input_edit_expired_date.text.startsWith("1")) {
            var input = input_edit_expired_date.text.toString()
            input = input.replace("/", "");
            val buffer = StringBuffer("0$input")
            buffer.insert(buffer.length - 2, "/")
            input_edit_expired_date.text.replace(0, input_edit_expired_date.text.length, buffer.toString())
        }
    }


    private fun setKeyboardListeners() {
        input_edit_card_number.setOnEditorActionListener({ _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                validateCreditCardNumber()
                handled = true
            }
            handled
        })
        input_edit_expired_date.setOnEditorActionListener({ _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                optionallyFillExpirationDateMonth()
                mPresenter.validateCreditCardExpiryDate(input_edit_expired_date.text.toString())
                handled = true
            }
            handled
        })
        input_edit_card_holder.setOnEditorActionListener({ _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                mPresenter.validateCreditCardHolder(input_edit_card_holder.text.toString())
                handled = true
            }
            handled
        })
        input_edit_cvv_code.setOnEditorActionListener({ _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                mPresenter.validateCreditCardCVV(input_edit_cvv_code.text.toString())
                handled = true
            }
            handled
        })
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
            stateMachine.nextState(activity!!)
        }
    }

    private fun setViewsIfCardIsNotNull() {
        card?.let {
            with(card!!) {
                input_edit_card_number.setText(number)
                input_edit_expired_date.setText(expiryDate)
                input_edit_cvv_code.setText(cvc)
                val priority = resources.getString(if (isPrimary?.let { it } == true)
                    R.string.credit_card_priority_primary
                else
                    R.string.credit_card_priority_secondary
                )
                credit_card_priority.setText(priority)
                credit_card_type.setText(type)
                mPresenter.getCreditCardLogo(company ?: "")
            }
            validateCreditCardNumber() //to flip to active state if it is during edit
        }
    }

    private fun validateCreditCardNumber() {
        mPresenter.validateCreditCardNumber(
                input_edit_card_number.text.removeNotDigits().toString()
        )
    }

    private fun CharSequence.removeNotDigits() = filter { it.isDigit() }

    private fun prettyFormatCreditCardNumber(s: String): String {
        val sb = StringBuilder(s)
        //add whitespace after each 4 characters
        (1..(sb.length - 1) / 4).map { distanceLength ->
            sb.insert(distanceLength * 4 + SPACING_CREDIT_NUMBER.length * (distanceLength - 1), SPACING_CREDIT_NUMBER)
        }
        return sb.toString()
    }

    private fun setCreditCardLogoAppearance(logoDrawable: Drawable?, gradientDrawable: Drawable?) {
        if (!inSet!!.isRunning && !outSet!!.isRunning) {
            gradientDrawable?.let {
                credit_card_background_active_front_side.background = it
                credit_card_background_active_bottom_side.background = it
            }
            front_card_logo.setImageDrawable(logoDrawable)
            bottom_card_logo.setImageDrawable(logoDrawable)
        }
    }

    private fun showKeyboard(activity: Activity, view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    private fun updateProgressBar(progress: Int) {
        val animation = ObjectAnimator.ofInt(progress_horizontal, "progress", progress)
        animation.duration = 300
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    private fun save() {
        view_pager.currentItem = 4
        card = card?.let { it } ?: CreditCard()
        with(card!!) {
            number = input_edit_card_number.text.toString()
            expiryDate = input_edit_expired_date.text.toString()
            holderName = input_edit_card_holder.text.toString()
            cvc = input_edit_cvv_code.text.toString()
        }
        val creditCardType = credit_card_type.text.toString()
        val creditCardPriority = credit_card_priority.text.toString()

        save_card_container.showNext()
        mPresenter.saveCreditCard(number = input_edit_card_number.text.toString(),
                holderName = input_edit_card_holder.text.toString(),
                expiryDate = input_edit_expired_date.text.toString(),
                cvv = input_edit_cvv_code.text.toString(),
                isAirplus = card?.let {
                    it.type == CreditCardEnum.AIRPLUS.naming
                } ?: false,
                type = when (creditCardType) {
                    resources.getString(CardType.PERSONAL.textRes) -> AddEditCardContract.CardType.PERSONAL
                    resources.getString(CardType.BUSINESS.textRes) -> AddEditCardContract.CardType.BUSINESS
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
     * @param toActive if true animates from inactive to active, otherwise in reverse direction
     */
    private fun flipBetweenActiveFrontAndInactiveAppearance(logoDrawable: Drawable?,
                                                            gradientDrawable: Drawable?,
                                                            toActive: Boolean) {
        if (!outSet!!.isRunning && !inSet!!.isRunning) {
            setCreditCardLogoAppearance(logoDrawable, gradientDrawable)
            if ((!showingActiveFront && toActive || showingActiveFront && !toActive)) {
                mNextMenuItem.isVisible = toActive
                showingActiveFront = toActive

                credit_card_inactive.cardElevation = 0f
                credit_card_active_front_side.cardElevation = 0f

                val (outTarget, inTarget) = listOf(credit_card_inactive, credit_card_active_front_side).let {
                    if (toActive) it else it.reversed()
                }
                val checkCardNumber = {
                    //though it is called onAnimationEnd, isRunning still can return true, so we add it to message queue
                    Handler().post {
                        mPresenter.getCreditCardLogo(input_edit_card_number.text.removeNotDigits().toString())
                    }
                }
                outSet!!.setTarget(outTarget)
                outSet!!.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (context != null) {
                            checkCardNumber()
                        }
                    }
                })

                outSet!!.start()

                inSet!!.setTarget(inTarget)
                inSet!!.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (context != null) {
                            inTarget.cardElevation = context!!.dip(12f).toFloat()
                            checkCardNumber()
                        }
                    }
                })
                inSet!!.start()
            }
        }
    }

    /**
     * @param toBottom if true animates from active front to active bottom, otherwise in reverse direction
     */
    private fun flipBetweenActiveFrontAndActiveBottomAppearance(toBottom: Boolean) {
        if (!outSet!!.isRunning && !inSet!!.isRunning) {
            showingBottomFront = toBottom

            credit_card_inactive.cardElevation = 0f
            credit_card_active_front_side.cardElevation = 0f
            credit_card_active_bottom_side.cardElevation = 0f

            val (outTarget, inTarget) = listOf(credit_card_active_front_side, credit_card_active_bottom_side).let {
                if (toBottom) it else it.reversed()
            }
            val checkCurrentPosition = {
                Handler().post {
                    val currentState = stateMachine.currentState()
                    if (showingBottomFront && currentState == CardFlowState.HOLDER) {
                        flipBetweenActiveFrontAndActiveBottomAppearance(false)
                    } else if (!showingBottomFront && currentState == CardFlowState.CVV) {
                        flipBetweenActiveFrontAndActiveBottomAppearance(true)
                    }
                }
            }
            outSet!!.setTarget(outTarget)
            outSet!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (context != null) {
                        checkCurrentPosition()
                    }
                }
            })
            outSet!!.start()

            inSet!!.setTarget(inTarget)
            inSet!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (context != null) {
                        inTarget.cardElevation = context!!.dip(12f).toFloat()
                        checkCurrentPosition()
                    }
                }
            })
            inSet!!.start()
        }

    }

    private inner class MyPagerAdapter(val activity: WeakReference<Activity>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var resId = 0
            when (position) {
                0 -> resId = R.id.input_layout_card_number
                1 -> resId = R.id.input_layout_expired_date
                2 -> resId = R.id.input_layout_card_holder
                3 -> resId = R.id.input_layout_cvv_code
                4 -> resId = R.id.space
            }
            return activity.get()!!.findViewById(resId)
        }


        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}


        override fun getCount(): Int {
            return 5
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }


    private inner class CardFlowStateMachine {
        private var state: CardFlowState? = CardFlowState.CARD_NUMBER

        fun nextState(activity: Activity) {
            when (state) {
                CardFlowState.CARD_NUMBER -> run {
                    view_pager.currentItem = 1
                }
                CardFlowState.EXPIRATION -> run {
                    view_pager.currentItem = 2
                }
                CardFlowState.HOLDER -> run {
                    flipBetweenActiveFrontAndActiveBottomAppearance(toBottom = true)
                    view_pager.currentItem = 3
                }
                CardFlowState.CVV -> run {
                    mNextMenuItem.isVisible = false
                    add_edit_card_steps_view_flipper.displayedChild = PositionInViewFlipper.CREDIT_CARD_TYPE_PRIORITY
                    activity.closeSoftKeyboard()
                }
                CardFlowState.TYPE_PRIORITY -> run {
                    add_edit_card_steps_view_flipper.displayedChild = PositionInViewFlipper.CREDIT_CARD_SUCCESS
                }
                CardFlowState.SUCCESS -> run {
                    activity.finish()
                }
            }
            changeStateToNext()
        }

        fun previousState(activity: Activity) {
            when (state) {
                CardFlowState.CARD_NUMBER -> run {
                    activity.finish()
                }
                CardFlowState.EXPIRATION -> run {
                    view_pager.currentItem = 0
                }
                CardFlowState.HOLDER -> run {
                    view_pager.currentItem = 1
                }
                CardFlowState.CVV -> run {
                    flipBetweenActiveFrontAndActiveBottomAppearance(toBottom = false)
                    view_pager.currentItem = 2
                }
                CardFlowState.TYPE_PRIORITY -> run {
                    mNextMenuItem.isVisible = true
                    add_edit_card_steps_view_flipper.displayedChild = PositionInViewFlipper.CREDIT_CARD_NUMBER_HOLDER_CVV
                    input_edit_cvv_code.requestFocus()
                    showKeyboard(activity, input_edit_cvv_code)
                }
                CardFlowState.SUCCESS -> run {
                    activity.finish()
                }
            }
            changeStateToPrevious()
        }

        fun currentState() = state

        private fun changeStateToNext() {
            state = when (state) {
                CardFlowState.CARD_NUMBER -> CardFlowState.EXPIRATION
                CardFlowState.EXPIRATION -> CardFlowState.HOLDER
                CardFlowState.HOLDER -> CardFlowState.CVV
                CardFlowState.CVV -> CardFlowState.TYPE_PRIORITY
                CardFlowState.TYPE_PRIORITY -> CardFlowState.SUCCESS
                CardFlowState.SUCCESS -> null
                null -> null
            }
        }

        private fun changeStateToPrevious() {
            state = when (state) {
                CardFlowState.CARD_NUMBER -> null
                CardFlowState.EXPIRATION -> CardFlowState.CARD_NUMBER
                CardFlowState.HOLDER -> CardFlowState.EXPIRATION
                CardFlowState.CVV -> CardFlowState.HOLDER
                CardFlowState.TYPE_PRIORITY -> CardFlowState.CVV
                CardFlowState.SUCCESS -> CardFlowState.TYPE_PRIORITY
                null -> null
            }
        }
    }

    private enum class CardFlowState {
        CARD_NUMBER, EXPIRATION, HOLDER, CVV, TYPE_PRIORITY, SUCCESS;
    }

    private enum class CardType(@StringRes val textRes: Int) {
        PERSONAL(R.string.credit_card_type_personal), BUSINESS(R.string.credit_card_type_business)
    }

    private enum class CardPriority(@StringRes val textRes: Int) {
        PRIMARY(R.string.credit_card_priority_primary), SECONDARY(R.string.credit_card_priority_secondary)
    }

    private fun hasUserAddedCreditCardDetails(): Boolean {
        val number = input_edit_card_number.text.toString()
        val holderName = input_edit_card_holder.text.toString()
        val expiryDate = input_edit_expired_date.text.toString()
        val cvv = input_edit_cvv_code.text.toString()
        return !TextUtils.isEmpty(number)
                || !TextUtils.isEmpty(holderName)
                || !TextUtils.isEmpty(expiryDate)
                || !TextUtils.isEmpty(cvv)
    }

}