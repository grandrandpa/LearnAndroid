package top.shixinzhang.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private Observable mObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        createObservables();
//        createObservableWithInterval();
//        createObservableWithJust();
//        createObservableWithRange();
        createObservableWithRepeat();

        errorHandlerWithRetry();
    }

    /**
     * 错误重试
     * 在遇到需要重复时，抛出一个自定义的异常，在紧接着的链上使用retryWhen上判断是否是这个异常
     * http://www.jianshu.com/p/023a5f60e6d0
     */
    private void errorHandlerWithRetry() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {

            }
        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (throwable instanceof IOException) { //是否是指定的异常
                            return Observable.just(1);
                        } else {
                            return Observable.error(throwable);
                        }
                    }
                });
            }
        });
    }

    private int repeatCount;

    private void createObservableWithRepeat() {
        String[] words = {"shixin", "is", "cute"};
        Observable<String> from = Observable.from(words);
//        from.repeat(2)

        Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                for (int i = 0; i < 5; i++) {
                    if(i == 2){
                        subscriber.onError(new Throwable("error")); //发射 onError 后会停止重复
                    }else {
                        subscriber.onNext("item " + i);
                    }
                }
                subscriber.onCompleted();
            }
        }).repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
            @Override
            public Observable<?> call(final Observable<? extends Void> completed) {
                //每次调用 onCompleted，都会进入这里，需要在这里决定是否需要重订阅
                return completed.delay(5, TimeUnit.SECONDS);
            }
        }).subscribe(getPrintSubscriber());
    }

    private void createObservableWithRange() {
        Observable<Integer> range = Observable.range(3, 5);
        range.subscribe(this.<Integer>getPrintSubscriber());
    }

    private void createObservableWithJust() {
        String[] words = {"shixin", "is", "cute"};
        Observable<String[]> just = Observable.just(words);
        just.subscribe(getPrintSubscriber());

        Observable<Object> just1 = Observable.just(null);
        just1.subscribe(getPrintSubscriber());
    }

    private void createObservableWithInterval() {
        Observable<Long> interval = Observable.interval(1, TimeUnit.SECONDS);
        interval.subscribe(getPrintSubscriber());
    }

    private void createObservables() {
        String[] words = {"shixin", "is", "cute"};
        Observable<String> from = Observable.from(words);
        from.subscribe(getPrintSubscriber());
    }


    /**
     * 用于打印结果的订阅者
     *
     * @param <T>
     * @return
     */
    private <T> Subscriber<T> getPrintSubscriber() {
        return new Subscriber<T>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError: " + e.getMessage());
            }

            @Override
            public void onNext(T t) {
                System.out.println("onNext: " + t);
            }
        };
    }

}
