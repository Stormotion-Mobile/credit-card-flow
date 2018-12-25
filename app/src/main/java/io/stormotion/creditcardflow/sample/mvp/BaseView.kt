package io.stormotion.creditcardflow.sample.mvp

interface BaseView<in T> {
    fun setPresenter(presenter: T)
}