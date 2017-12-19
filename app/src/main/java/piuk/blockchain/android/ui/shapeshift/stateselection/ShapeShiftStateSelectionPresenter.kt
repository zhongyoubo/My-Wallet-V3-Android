package piuk.blockchain.android.ui.shapeshift.stateselection

import android.app.Activity
import info.blockchain.wallet.shapeshift.data.State
import io.reactivex.Completable
import piuk.blockchain.android.R
import piuk.blockchain.android.data.rxjava.RxUtil
import piuk.blockchain.android.data.shapeshift.ShapeShiftDataManager
import piuk.blockchain.android.data.walletoptions.WalletOptionsDataManager
import piuk.blockchain.android.ui.base.BasePresenter
import piuk.blockchain.android.util.americanStatesMap
import piuk.blockchain.android.util.annotations.Mockable
import timber.log.Timber
import javax.inject.Inject

@Mockable
class ShapeShiftStateSelectionPresenter @Inject constructor(
        private val walletOptionsDataManager: WalletOptionsDataManager,
        private val shapeShiftDataManager: ShapeShiftDataManager
) : BasePresenter<ShapeShiftStateSelectionView>() {

    override fun onViewReady() {
        // No-op
    }

    internal fun updateAmericanState(state: String) {
        val stateCode = americanStatesMap[state]
        require(stateCode != null) { "State not found in map" }

        walletOptionsDataManager.isStateWhitelisted(stateCode!!)
                .compose(RxUtil.addObservableToCompositeDisposable(this))
                .flatMapCompletable { whitelisted ->
                    if (whitelisted) {
                        shapeShiftDataManager.setState(State(state, stateCode))
                                .doOnComplete { view.finishActivityWithResult(Activity.RESULT_OK) }
                                .doOnError { view.finishActivityWithResult(Activity.RESULT_CANCELED) }
                    } else {
                        view.onError(R.string.shapeshift_unavailable_in_state)
                        Completable.complete()
                    }
                }
                .subscribe(
                        { /* No-op */ },
                        {
                            Timber.e(it)
                            view.finishActivityWithResult(Activity.RESULT_CANCELED)
                        }
                )
    }
}
