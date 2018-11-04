package io.stormotion.creditcardflow.mvp

interface BaseView<in T> {
    fun setPresenter(presenter: T)
}