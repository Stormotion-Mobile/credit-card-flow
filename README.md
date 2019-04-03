# CreditCardFlow

Library that can help you to add/edit credit cards in your app. For better UX it replicates an actual credit card, both front and bottom side. Supports Visa, Master Card, American Express, Discover, Diners Club Card.

## Screenshots

![alt text](https://github.com/Stormotion-Mobile/credit-card-flow/blob/master/images/library_demonstration.png)


## Adding to your project

You just need to add the following to your build.gradle file:
```groovy
dependencies {
  implementation 'io.stormotion:creditcardflow:1.0.0'
}
```

## Features

- Support for Visa, Master Card, American Express, Discover, Diners Club Card
- Replication of an actual credit card
- Beautiful animation when entering valid credit card number and when moving to the bottom of credit card
- Fully customizable texts
- Validation of each field: credit card, expiry date, card holder and cvc


## Usage

Just add it to your layout:

```xml
<io.stormotion.creditcardflow.CreditCardFlow 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/credit_card_flow"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## Public methods

| Method       | Description|
| ------------- |:-------------:| 
| getCvvInputEditText| Return edit text for input CVV |
| getCardNumberInputEditText     | Return edit text for input card number      |
| getCardHolderInputEditText | Return edit text for input CVV      |
| getCardExpiryDateInputEditText | Return edit text for input expiry date |
| setCreditCardFlowListener | Set CreditCardFlowListener for all changing actions |
| setInactiveCreditCardNumberHeaderStyle(Int) |Set Style for an inactive credit card number header |
| setInactiveCreditCardNumberValueStyle(Int) | Set Style for an inactive credit card number value|
| setInactiveCreditCardExpiryDateHeaderStyle(Int) | Set Style for an inactive credit card expiry date header|
| setInactiveCreditCardExpiryDateValueStyle(Int) | Set Style for an inactive credit card expiry date style|
| setInactiveCreditCardHolderHeaderStyle(Int) | Set Style for an inactive credit card holder header|
| setInactiveCreditCardHolderValueStyle(Int) | Set Style for an inactive credit card number value|
| setActiveCreditCardNumberHeaderStyle(Int) | Set Style for an active credit card number header|
| setActiveCreditCardNumberValueStyle(Int) | Set Style for an active credit card number value|
| setActiveCreditCardExpiryDateHeaderStyle(Int) | Set Style for an active credit card expiry date header|
| setActiveCreditCardExpiryDateValueStyle(Int) | Set Style for an active credit card expiry date value|
| setActiveCreditCardHolderHeaderStyle(Int) | Set Style for an active credit card holder header|
| setActiveCreditCardHolderValueStyle(Int) | Set Style for an active credit card holder value|
| setCardCvvStyle(Int) | Set Style for credit card cvv|
| setInputCreditCardNumberStyle(Int) | Set Style for a credit card number input|
| setInputCreditCardHolderStyle(Int) | Set Style for a credit card holder input|
| setInputCreditCardExpiryDateStyle(Int) | Set Style for a credit card expiry date input|
| setInputCreditCardCvvStyle(Int) | Set Style for a credit card cvv input| 
| setCreditCard(CreditCard) | Set CreditCard and immediatelly changes view|
| nextState | Move credit card to a next state manually|
| previousState | Move credit card to a previous state manually|
| currentState | Return one of values of CardFlowState: INACTIVE_CARD_NUMBER, ACTIVE_CARD_NUMBER, EXPIRATION, HOLDER, CVV|
| validateCreditCardNumber | Validate credit card number manually|
| validateCreditCardExpiryDate | Validate credit card expiry date manually|
| validateCreditCardHolder | Validate credit card holder manually|
| validateCreditCardCVV | Validate credit card cvv manually|
| creditCardType | Returns one of values of CreditCardEnum: VISA, AMERICAN_EXPRESS, MASTER_CARD, DISCOVER, DINERS_CLUB_CARD, UNKNOWN|
| creditCardNumberWithoutNotDigits | Return current value of credit card number with only digits|
| creditCardNumber | Return current value of credit card number with spaces|
| creditCardHolder | Return current value of credit card holder|
| creditCardExpiryDate | Return current value of credit card expiry date|
| creditCardCvvCode | Return current value of credit card cvv|

## CreditCardFlowListener

| Callback method       | Description|
| ------------- |:-------------:| 
| onInactiveCardNumberBeforeChangeToNext| Invoked before changing from a card number to an active card number|
| onActiveCardNumberBeforeChangeToNext| Invoked before changing from a card number to an active expiry date|
| onCardExpiryDateBeforeChangeToNext| Invoked before changing from a card expiry date to an active card holder|
| onCardHolderBeforeChangeToNext| Invoked before changing from a card expiry date to an cvv|
| onCardCvvBeforeChangeToNext| Invoked before changing from a card cvv to the end of flow|
| onInactiveCardNumberBeforeChangeToPrevious| Invoked before changing from an inactive card number to the end of flow|
| onActiveCardNumberBeforeChangeToPrevious| Invoked before changing from an active card number to an inactive card number|
| onCardExpiryDateBeforeChangeToPrevious| Invoked before changing from an expiry date to an active card number|
| onCardHolderBeforeChangeToPrevious|  Invoked before changing from a card holder to an expiry date|
| onCardCvvBeforeChangeToPrevious|  Invoked before changing from a cvv to an active card number|
| onCardNumberValidatedSuccessfully(cardNumber)|  Invoked when card number validated successfully|
| onCardNumberValidationFailed(cardNumber)| Invoked when card number failed to validate|
| onCardHolderValidatedSuccessfully(cardHolder)| Invoked when card holder validated successfully|
| onCardHolderValidationFailed(cardHolder)| Invoked when card holder failed to validate|
| onCardExpiryDateValidatedSuccessfully(expiryDate)| Invoked when expiry date validated successfully|
| onCardExpiryDateValidationFailed(expiryDate)| Invoked when expiry date failed to validate|
| onCardExpiryDateInThePast(expiryDate)| Invoked when expiry date failed to validate with an expiry date in the past|
| onCardCvvValidatedSuccessfully(cvv)| Invoked when card cvv validated successfully|
| onCardCvvValidationFailed(cvv)| Invoked when card cvv failed to validate|
| onFromActiveToInactiveAnimationStart| Invoked when moving from an active to inactive card |
| onFromInactiveToActiveAnimationStart| Invoked when moving from an inactive to an active card|
| onCreditCardFlowFinished(CreditCard)| Invoked when credit card flow finished: everything was validated successfully|

### Acknowledgements

dimsob's [library](https://github.com/dimsob/android-sumbit-credit-card-flow) to start with (including [Azis Pradana](https://material.uplabs.com/azippy) for his [animation](https://www.uplabs.com/posts/submit-credit-card-flow-gif-animation)).

### License 

```
Copyright 2019 Stormotion, Zasukha Dmitriy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
