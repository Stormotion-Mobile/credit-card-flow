package io.stormotion.creditcardflow.credit_card_flow

import android.animation.*
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import android.widget.TextView
import io.stormotion.creditcardflow.R
import io.stormotion.creditcardflow.afterTextChanged
import kotlinx.android.synthetic.main.credit_card_active_bottom_side.view.*
import kotlinx.android.synthetic.main.credit_card_active_front_side.view.*
import kotlinx.android.synthetic.main.credit_card_flow.view.*
import kotlinx.android.synthetic.main.credit_card_inactive_front_side.view.*
import kotlinx.android.synthetic.main.credit_card_type_and_priority.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast

class CreditCardFlow : RelativeLayout, CreditCardFlowContract.View {

    companion object {
        //spacing between 4 digits of credit number
        private const val SPACING_CREDIT_NUMBER = "   "
    }

    private var showingActiveFront = false //indicate if active front or inactive is shown
    private var showingBottomFront = false //indicate if active front or active bottom is shown
    private var inSet: AnimatorSet? = null
    private var outSet: AnimatorSet? = null
    private var mCreditCardFlowListener: CreditCardFlowListener? = null
    private var card: CreditCard? = null
    private var stateMachine: CardFlowStateMachine = CardFlowStateMachine()

    private lateinit var mPresenter: CreditCardFlowContract.Presenter


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mPresenter.subscribe()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPresenter.unsubscribe()
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
        stateMachine.nextState()
        mCreditCardFlowListener?.onCardNumberValidatedSuccessfully(creditCardNumber())
    }

    override fun showCreditCardNumberFailedToValidate() {
        context!!.toast(R.string.credit_card_number_is_not_valid)
        mCreditCardFlowListener?.onCardNumberValidationFailed(creditCardNumber())
    }

    override fun showCreditCardHolderValidatedSuccessfully() {
        stateMachine.nextState()
        mCreditCardFlowListener?.onCardHolderValidatedSuccessfully(creditCardHolder())
    }

    override fun showCreditCardHolderFailedToValidate() {
        context!!.toast(R.string.credit_card_holder_is_not_valid)
        mCreditCardFlowListener?.onCardHolderValidationFailed(creditCardHolder())

    }

    override fun showCreditCardExpiryDateValidatedSuccessfully() {
        stateMachine.nextState()
        mCreditCardFlowListener?.onCardExpiryDateValidatedSuccessfully(creditCardExpiryDate())
    }

    override fun showCreditCardExpiryDateFailedToValidate() {
        context!!.toast(R.string.credit_card_expiry_date_is_not_valid)
        mCreditCardFlowListener?.onCardExpiryDateValidationFailed(creditCardExpiryDate())
    }

    override fun showCreditCardExpiryDateIsAlreadyExpired() {
        context!!.toast(R.string.error_expiry_date_in_the_past)
        mCreditCardFlowListener?.onCardExpiryDateInThePast(creditCardExpiryDate())
    }

    override fun showCreditCardCvvValidatedSuccessfully() {
        stateMachine.nextState()
        mCreditCardFlowListener?.onCardCvvValidatedSuccessfully(creditCardCvvCode())
    }

    override fun showCreditCardCvvFailedToValidate() {
        context!!.toast(R.string.credit_card_cvv_is_not_valid)
        mCreditCardFlowListener?.onCardCvvValidationFailed(creditCardCvvCode())
    }

    fun setCreditCardFlowListener(creditCardFlowListener: CreditCardFlowListener) {
        mCreditCardFlowListener = creditCardFlowListener
    }

    @SuppressWarnings("unused")
    fun setActiveCreditCardNumberHeaderStyle(@StyleRes styleResId: Int) {
        active_card_number_header.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setActiveCreditCardNumberValueStyle(@StyleRes styleResId: Int) {
        active_card_number_value.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setInactiveCreditCardNumberHeaderStyle(@StyleRes styleResId: Int) {
        inactive_card_number_header.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setInactiveCreditCardNumberValueStyle(@StyleRes styleResId: Int) {
        inactive_card_number_value.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setActiveCreditCardExpiryDateHeaderStyle(@StyleRes styleResId: Int) {
        active_expiry_date_header.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setActiveCreditCardExpiryDateValueStyle(@StyleRes styleResId: Int) {
        active_expiry_date_value.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setInactiveCreditCardExpiryDateHeaderStyle(@StyleRes styleResId: Int) {
        inactive_expiry_date_header.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setInactiveCreditCardExpiryDateValueStyle(@StyleRes styleResId: Int) {
        inactive_expiry_date_value.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setActiveCreditCardHolderHeaderStyle(@StyleRes styleResId: Int) {
        active_card_holder_header.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setActiveCreditCardHolderValueStyle(@StyleRes styleResId: Int) {
        active_card_holder_value.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setInactiveCreditCardHolderHeaderStyle(@StyleRes styleResId: Int) {
        inactive_card_holder_header.setTextAppearanceCompat(styleResId)
    }

    @SuppressWarnings("unused")
    fun setInactiveCreditCardHolderValueStyle(@StyleRes styleResId: Int) {
        inactive_card_holder_value.setTextAppearanceCompat(styleResId)
    }

    fun setCardCvvStyle(@StyleRes styleResId: Int) {
        card_cvv.setTextAppearanceCompat(styleResId)
    }

    override fun setPresenter(presenter: CreditCardFlowContract.Presenter) {
        mPresenter = presenter
    }

    @SuppressWarnings("unused")
    fun setCreditCard(creditCard: CreditCard) {
        card = creditCard
    }

    @SuppressWarnings("unused")
    fun nextState() = stateMachine.nextState()

    fun previousState() = stateMachine.previousState()

    fun currentState() = stateMachine.currentState()

    fun validateCreditCardNumber() {
        mPresenter.validateCreditCardNumber(creditCardNumber().removeNotDigits().toString())
    }

    fun validateCreditCardExpiryDate() {
        mPresenter.validateCreditCardExpiryDate(creditCardExpiryDate())
    }

    fun validateCreditCardHolder() {
        mPresenter.validateCreditCardHolder(creditCardHolder())
    }

    fun validateCreditCardCVV() {
        mPresenter.validateCreditCardCVV(creditCardCvvCode())
    }

    @SuppressWarnings("unused")
    fun creditCardType() = CreditCardEnum.getCreditCardByNumber(creditCardNumberWithoutNotDigits())

    fun creditCardNumberWithoutNotDigits() = creditCardNumber().removeNotDigits().toString()

    fun creditCardNumber() = input_edit_card_number.text.toString()

    fun creditCardHolder() = input_edit_card_holder.text.toString()

    fun creditCardExpiryDate() = input_edit_expired_date.text.toString()

    fun creditCardCvvCode() = input_edit_cvv_code.text.toString()

    private fun init() {
        View.inflate(context, R.layout.credit_card_flow, this)

        CreditCardFlowPresenter(this)

        setTextListeners()
        configureViewPager()
        setKeyboardListeners()

        input_edit_card_number.requestFocus()

        inSet = AnimatorInflater.loadAnimator(context, R.animator.card_flip_in) as AnimatorSet
        outSet = AnimatorInflater.loadAnimator(context, R.animator.card_flip_out) as AnimatorSet

        setViewsIfCardIsNotNull()
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
                active_card_number_value.text = s.toString()
                inactive_card_number_value.text = s.toString().replace(SPACING_CREDIT_NUMBER, " ")
                cardNumberLock = false
            } else if (!cardNumberLock) {
                cardNumberLock = true
                val numberCharactersToRemove = noWhiteSpacesCardNumber.length - maxCreditCardNumberLength
                val savedCharacters = noWhiteSpacesCardNumber.removeRange(startIndex = noWhiteSpacesCardNumber.length - numberCharactersToRemove,
                        endIndex = noWhiteSpacesCardNumber.length)
                s.replace(0, s.length, prettyFormatCreditCardNumber(savedCharacters))
                mPresenter.getCreditCardLogo(noWhiteSpacesCardNumber)
                active_card_number_value.text = s.toString()
                inactive_card_number_value.text = s.toString().replace(SPACING_CREDIT_NUMBER, " ")
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
                active_card_holder_value.text = s.toString()
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
            active_expiry_date_value.text = expiredDate
        }

        input_edit_cvv_code.afterTextChanged { s ->
            card_cvv.text = s.toString()
            card_cvv.inputType = if (s.toString().isEmpty()) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            } else {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }

        }
    }

    private fun configureViewPager() {
        val adapter = MyPagerAdapter()
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
            var input = creditCardExpiryDate()
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
                validateCreditCardExpiryDate()
                handled = true
            }
            handled
        })
        input_edit_card_holder.setOnEditorActionListener({ _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                validateCreditCardHolder()
                handled = true
            }
            handled
        })
        input_edit_cvv_code.setOnEditorActionListener({ _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                validateCreditCardCVV()
                handled = true
            }
            handled
        })
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

    private fun updateProgressBar(progress: Int) {
        val animation = ObjectAnimator.ofInt(progress_horizontal, "progress", progress)
        animation.duration = 300
        animation.interpolator = DecelerateInterpolator()
        animation.start()
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
                showingActiveFront = toActive

                credit_card_inactive.cardElevation = 0f
                credit_card_active_front_side.cardElevation = 0f

                val (outTarget, inTarget) = listOf(credit_card_inactive, credit_card_active_front_side).let {
                    if (toActive) it else it.reversed()
                }
                val checkCardNumber = {
                    //though it is called onAnimationEnd, isRunning still can return true, so we add it to message queue
                    Handler().post {
                        mPresenter.getCreditCardLogo(creditCardNumberWithoutNotDigits())
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

                if (toActive) {
                    mCreditCardFlowListener?.onFromInactiveToActiveAnimationStart()
                } else {
                    mCreditCardFlowListener?.onFromActiveToInactiveAnimationStart()
                }
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

    private fun TextView.setTextAppearanceCompat(styleResId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(styleResId)
        } else {
            setTextAppearance(context, styleResId)
        }
    }

    private inner class MyPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var resId = 0
            when (position) {
                0 -> resId = R.id.input_layout_card_number
                1 -> resId = R.id.input_layout_expired_date
                2 -> resId = R.id.input_layout_card_holder
                3 -> resId = R.id.input_layout_cvv_code
                4 -> resId = R.id.space
            }
            return findViewById(resId)
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
        private var state: CardFlowState = CardFlowState.CARD_NUMBER

        fun nextState() {
            when (state) {
                CardFlowState.CARD_NUMBER -> run {
                    view_pager.currentItem = 1
                    mCreditCardFlowListener?.onCardNumberBeforeChangeToNext()
                }
                CardFlowState.EXPIRATION -> run {
                    view_pager.currentItem = 2
                    mCreditCardFlowListener?.onCardExpiryDateBeforeChangeToNext()
                }
                CardFlowState.HOLDER -> run {
                    flipBetweenActiveFrontAndActiveBottomAppearance(toBottom = true)
                    view_pager.currentItem = 3
                    mCreditCardFlowListener?.onCardHolderBeforeChangeToNext()
                }
                CardFlowState.CVV -> run {
                    mCreditCardFlowListener?.onCardCvvBeforeChangeToNext()
                }
            }
            changeStateToNext()
        }

        fun previousState() {
            when (state) {
                CardFlowState.CARD_NUMBER -> run {
                    mCreditCardFlowListener?.onCardNumberBeforeChangeToPrevious()
                }
                CardFlowState.EXPIRATION -> run {
                    view_pager.currentItem = 0
                    mCreditCardFlowListener?.onCardExpiryDateBeforeChangeToPrevious()
                }
                CardFlowState.HOLDER -> run {
                    view_pager.currentItem = 1
                    mCreditCardFlowListener?.onCardHolderBeforeChangeToPrevious()
                }
                CardFlowState.CVV -> run {
                    flipBetweenActiveFrontAndActiveBottomAppearance(toBottom = false)
                    view_pager.currentItem = 2
                    mCreditCardFlowListener?.onCardCvvBeforeChangeToPrevious()
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
                CardFlowState.CVV -> CardFlowState.CVV
            }
        }

        private fun changeStateToPrevious() {
            state = when (state) {
                CardFlowState.CARD_NUMBER -> CardFlowState.CARD_NUMBER
                CardFlowState.EXPIRATION -> CardFlowState.CARD_NUMBER
                CardFlowState.HOLDER -> CardFlowState.EXPIRATION
                CardFlowState.CVV -> CardFlowState.HOLDER
            }
        }
    }
}
