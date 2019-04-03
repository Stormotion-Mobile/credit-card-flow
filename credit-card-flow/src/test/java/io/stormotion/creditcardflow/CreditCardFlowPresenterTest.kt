package io.stormotion.creditcardflow

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import java.util.*

class CreditCardFlowPresenterTest {
    private lateinit var presenter: CreditCardFlowPresenter
    private val view = mock<CreditCardFlowContract.View>()

    private val creditCardNumbers = mapOf(
            CreditCardEnum.AMERICAN_EXPRESS to listOf("371449635398431",
                    "346221524320194",
                    "375303789736811",
                    "349033669366818",
                    "374449666093965",
                    "347416425817921"),
            CreditCardEnum.DINERS_CLUB_CARD to listOf("36700102000000",
                    "36148900647913",
                    "30569309025904",
                    "30144189586936",
                    "30078727433633",
                    "30068783171060",
                    "30282579749464"),
            CreditCardEnum.DISCOVER to listOf("6011111111111117",
                    "6011066575218560",
                    "6011914999687276",
                    "6011240433711146",
                    "6011863194218975",
                    "6011502609580526"),
            CreditCardEnum.MASTER_CARD to listOf("5555555555554444",
                    "5454545454545454",
                    "5242732587924981",
                    "5418954133015241",
                    "5413500492163281",
                    "5416802819055837",
                    "5127722154119776"),
            CreditCardEnum.VISA to listOf("4444333322221111",
                    "4911830000000",
                    "4917610000000000",
                    "4462030000000000",
                    "4462030000000000",
                    "4917300800000000",
                    "4484070000000000",
                    "4111111111111111",
                    "4716058083563507",
                    "4799171063814632",
                    "4556025279664244",
                    "4532354978578698",
                    "4673370507420425"),
            CreditCardEnum.UNKNOWN to listOf("3528000700000000",
                    "3528206150246080")
    )

    @Before
    fun initTest() {
        presenter = CreditCardFlowPresenter(view)
    }

    @Test
    fun createPresenter_setsPresenterToView() {
        verify(view).setPresenter(presenter)
    }

    @Test
    fun getCreditCardLogo_showCreditCardLogoWhenAmericanExpress() {
        getCreditCardLogo_showCreditCardLogo(CreditCardEnum.AMERICAN_EXPRESS)
    }

    @Test
    fun getCreditCardLogo_showCreditCardLogoWhenDiners() {
        getCreditCardLogo_showCreditCardLogo(CreditCardEnum.DINERS_CLUB_CARD)
    }

    @Test
    fun getCreditCardLogo_showCreditCardLogoWhenDiscover() {
        getCreditCardLogo_showCreditCardLogo(CreditCardEnum.DISCOVER)
    }

    @Test
    fun getCreditCardLogo_showCreditCardLogoWhenMastercard() {
        getCreditCardLogo_showCreditCardLogo(CreditCardEnum.MASTER_CARD)
    }

    @Test
    fun getCreditCardLogo_showCreditCardLogoWhenVisa() {
        getCreditCardLogo_showCreditCardLogo(CreditCardEnum.VISA)
    }

    @Test
    fun getCreditCardLogo_showCreditCardLogoWhenUnknown() {
        val list = creditCardNumbers[CreditCardEnum.UNKNOWN]!!
        list.forEach { presenter.getCreditCardLogo(it) }
        verify(view, times(list.size)).showNoCreditCardLogo()
    }

    @Test
    fun validateCreditCardNumber_showCreditCardNumberValidatedSuccessfully() {
        val list = creditCardNumbers.filterKeys { it != CreditCardEnum.UNKNOWN }.values.flatten()
        list.forEach { presenter.validateCreditCardNumber(it) }
        verify(view, times(list.size)).showCreditCardNumberValidatedSuccessfully()
    }

    @Test
    fun validateCreditCardNumber_showCreditCardNumberFailedToValidate() {
        val list = creditCardNumbers[CreditCardEnum.UNKNOWN]!!
        list.forEach { presenter.validateCreditCardNumber(it) }
        verify(view, times(list.size)).showCreditCardNumberFailedToValidate()
    }

    @Test
    fun validateCreditCardHolder_showCreditCardHolderValidatedSuccessfullyWhenOnlyFirstName() {
        presenter.validateCreditCardHolder("FirstName")
        verify(view).showCreditCardHolderValidatedSuccessfully()
    }

    @Test
    fun validateCreditCardHolder_showCreditCardHolderValidatedSuccessfullyWhenFirstNameAndLastName() {
        presenter.validateCreditCardHolder("FirstName LastName")
        verify(view).showCreditCardHolderValidatedSuccessfully()
    }

    @Test
    fun validateCreditCardHolder_showCreditCardHolderValidatedSuccessfullyWhenFirstNameAndLastNameAndMiddleName() {
        presenter.validateCreditCardHolder("FirstName MiddleName LastName")
        verify(view).showCreditCardHolderValidatedSuccessfully()
    }

    @Test
    fun validateCreditCardHolder_showCreditCardHolderFailedToValidateWhenNumbers() {
        presenter.validateCreditCardHolder("FirstName1")
        presenter.validateCreditCardHolder("FirstName LastName1")
        presenter.validateCreditCardHolder("FirstName MiddleName1 LastName")
        verify(view, times(3)).showCreditCardHolderFailedToValidate()
    }

    @Test
    fun validateCreditCardHolder_showCreditCardHolderFailedToValidateWhenEmptyString() {
        presenter.validateCreditCardHolder("")
        verify(view, times(1)).showCreditCardHolderFailedToValidate()
    }


    @Test
    fun validateCreditCardHolder_showCreditCardHolderFailedToValidateWhenMoreThanOneWhitespaceBetweenWords() {
        presenter.validateCreditCardHolder("FirstName  LastName")
        presenter.validateCreditCardHolder("FirstName MiddleName  LastName")
        verify(view, times(2)).showCreditCardHolderFailedToValidate()
    }

    @Test
    fun validateCreditCardExpiryDate_showCreditCardExpiryDateValidatedSuccessfully() {
        val allValidExpiryDates = (1..12).map { if (it / 10.0 >= 1) it.toString() else "0$it" }.map { month ->
            (Calendar.getInstance().get(Calendar.YEAR) + 1..99).map { year -> if (year / 10.0 >= 1) "$month/$year" else "$month/0$year" }
        }.flatten()
        allValidExpiryDates.forEach { expiryDate -> presenter.validateCreditCardExpiryDate(expiryDate) }
        verify(view, times(allValidExpiryDates.size)).showCreditCardExpiryDateValidatedSuccessfully()
    }

    @Test
    fun validateCreditCardExpiryDate_showCreditCardExpiryDateFailedToValidate() {
        val notValidExpiryDates = listOf("0213",
                "022013",
                "02/2013",
                "02/203",
                "02/2",
                "02/20322")
        notValidExpiryDates.forEach { expiryDate -> presenter.validateCreditCardExpiryDate(expiryDate) }
        verify(view, times(notValidExpiryDates.size)).showCreditCardExpiryDateFailedToValidate()
    }


    @Test
    fun validateCreditCardExpiryDate_showCreditCardExpiryDateAlreadyExpired() {
        val notValidExpiryDates = listOf("02/17", "01/11", "05/18")
        notValidExpiryDates.forEach { expiryDate -> presenter.validateCreditCardExpiryDate(expiryDate) }
        verify(view, times(notValidExpiryDates.size)).showCreditCardExpiryDateIsAlreadyExpired()
    }

    @Test
    fun validateCreditCardCVV_showCreditCardCvvValidatedSuccessfullyThreeNumbers() {
        presenter.validateCreditCardCVV("123")
        verify(view).showCreditCardCvvValidatedSuccessfully()
    }

    @Test
    fun validateCreditCardCVV_showCreditCardCvvValidatedSuccessfullyFourNumbers() {
        presenter.validateCreditCardCVV("1234")
        verify(view).showCreditCardCvvValidatedSuccessfully()
    }

    @Test
    fun validateCreditCardCVV_showCreditCardCvvFailedToValidateWhenNumberOfNumbersOtherThanThreeOrFour() {
        val notValidCvv = listOf("12345",
                "12",
                "ahbdb",
                "abc",
                "abcd",
                "1",
                "",
                "    ",
                "   ")
        notValidCvv.forEach { presenter.validateCreditCardCVV(it) }
        verify(view, times(notValidCvv.size)).showCreditCardCvvFailedToValidate()
    }

    private fun getCreditCardLogo_showCreditCardLogo(creditCard: CreditCardEnum) {
        val list = creditCardNumbers[creditCard]!!
        list.forEach { presenter.getCreditCardLogo(it) }
        verify(view, times(list.size)).showCreditCardLogo(creditCard)
    }

}