package observable.base;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import observable.base.bean.Student;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.*;

public class Base {
    private static List<Student> studentList = new ArrayList<>();

    static {
        studentList.add(new Student("Ben", 1, 11));
        studentList.add(new Student("Mike", 2, 11));
        studentList.add(new Student("Susan", 3, 11));
        studentList.add(new Student("Kate", 1, 12));
        studentList.add(new Student("Tom", 1, 13));
        studentList.add(new Student("John", 2, 12));
        studentList.add(new Student("Leo", 3, 13));
        studentList.add(new Student("Sam", 2, 13));

    }

    public static void main(String[] args) {
        System.out.println("hello world");
//        interval();
//        range();
//        createBase();
//        defer();
//        emptyErrorNever();
//        from();
//        just();
//        repeat();
//        timer();
//        mapCast();
//        flatMapContactMap();
//        flatMapIterable();
//        buffer();
//        groupBy();
//        scan();
//        window();
//        filter();
//        element();
//        distinct();
//        skip();
//        take();
        throttle();
    }

    public static void interval() {
        /*
        Observable.interval(1, TimeUnit.SECONDS)
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(new Consumer<Long>() {
        @Override
        public void accept(Long aLong) throws Exception {
        ToastHelper.getInstance().show(aLong.toString());
        }
        });
         */
        //android中以上代码可以直接执行，这里interval方法中要加入第三个参数才能执行。
        //每隔三秒执行
        Observable.interval(3, TimeUnit.SECONDS, Schedulers.trampoline())
                .subscribe(System.out::println);
        Observable.intervalRange(0, 10, 1, 2, TimeUnit.SECONDS, Schedulers.trampoline())
                .subscribe(System.out::println);


    }

    public static void range() {
        Observable.range(5, 4)
                .subscribe(System.out::println);
    }

    public static void createBase() {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            emitter.onNext("123");
            emitter.onNext("321");
            emitter.onComplete();
        }).subscribe(System.out::println);
    }

    /*defer直到有观察者订阅时才创建Observable，并且为每个观察者创建一个新的Observable。defer操作符会一直等待直到有观察者订阅它，
       然后它使用Observable工厂方法生成一个Observable。比如下面的代码两个订阅输出的结果是不一致的：
     */
    public static void defer() {
        Observable observable = Observable.defer((Callable<ObservableSource<String>>) () -> Observable.just(String.valueOf(System.currentTimeMillis())));

        observable.subscribe(System.out::println);
        observable.subscribe(System.out::println);
        Observable observable1 = Observable.create((ObservableOnSubscribe<String>) em -> {
            em.onNext(String.valueOf(System.currentTimeMillis()));
            em.onComplete();
        });
        System.out.println(String.valueOf(System.currentTimeMillis()));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        observable1.subscribe(System.out::println);
        observable1.subscribe(System.out::println);
    }

    /**
     * empty:doOnTerminate
     * empty:onComplete
     * error:doOnTerminate
     * error:onError
     * //TODO   doOnTerminate触发的比onComplete和onError还要早？
     */
    public static void emptyErrorNever() {
        Observable.<String>empty().doOnTerminate(() -> System.out.println("empty:doOnTerminate")).subscribe(string -> System.out.println("empty:onNext"), e -> System.out.println("empty:onError"), () -> System.out.println("empty:onComplete"));
        Observable.error(() -> new Throwable()).doOnTerminate(() -> System.out.println("error:doOnTerminate")).subscribe(o -> System.out.println("error:onNext"), throwable -> System.out.println("error:onError"), () -> System.out.println("error:onComplete"));
        Observable.never().doOnTerminate(() -> System.out.println("never:doOnTerminate")).subscribe(o -> System.out.println("never:onNext"), throwable -> System.out.println("never:onError"), () -> System.out.println("never:onComplete"));
    }

    public static void from() {
        String[] strings = {"aaa", "bbb", "ccc"};
        Observable.fromArray(strings).subscribe(System.out::println);

        Observable.fromCallable(() -> String.valueOf(System.currentTimeMillis())).subscribe(System.out::println);

        FutureTask<String> futureTask = new FutureTask<>(() -> String.valueOf(System.currentTimeMillis()));
        Future<String> future = Executors.newCachedThreadPool().submit(() -> String.valueOf(System.currentTimeMillis()));
        Observable.fromFuture(future)
                .subscribe(System.out::println);

        //TODO 此处警告如何处理？
        Observable<String> observable = Observable.<String>fromFuture(futureTask)
                .doOnSubscribe(disposable -> {
//                    if (System.currentTimeMillis() % 2 == 0) {
                    futureTask.run();
//                    } else futureTask.cancel(true);
                });
        observable.subscribe(s -> System.out.println("future:onNext" + s));
//        futureTask.run();
        List<String> stringList = Arrays.asList(strings);
        Observable.fromIterable(stringList).subscribe(System.out::println);

        Observable.fromPublisher((Publisher<String>) s -> {
            s.onNext("eee");
            s.onComplete();
        }).subscribe(a -> System.out.println("publisher:onNext" + a), throwable -> System.out.println("publisher:onError"), () -> System.out.println("publisher:onComplete"));

    }

    //just参数有个数限制
    public static void just() {
        Observable.just("aaa", "bbb")
                .subscribe(s -> {
                    System.out.println("just:onNext," + s);
                });
    }

    public static void repeat() {
        Observable<Long> observable = Observable.intervalRange(5, 5, 1, 1, TimeUnit.SECONDS, Schedulers.trampoline());
//        observable.repeat().subscribe(s -> System.out.println("repeatForever:onNext," + s));
        observable.repeat(2).subscribe(System.out::println);
        observable.repeatUntil(() -> System.currentTimeMillis() / 1000 % 4 >= 3).subscribe(s -> System.out.println("repeatUntil:onNext," + s));
        observable.repeatWhen(objectObservable -> objectObservable.take(1)).subscribe(s -> System.out.println("repeatWhen:onNext" + s));

    }

    public static void timer() {
        Observable.timer(2, TimeUnit.SECONDS, Schedulers.trampoline()).subscribe(System.out::println);
    }

    public static void mapCast() {
        Observable.range(5, 5).map(integer -> String.valueOf(integer % 2)).subscribe(System.out::println);
        Observable.just(new Date()).cast(Object.class).subscribe(System.out::println);

    }

    public static void flatMapContactMap() {
        Observable<Integer> observable = Observable.range(5, 5);
        Observable.range(5, 5).flatMap((Function<Integer, ObservableSource<?>>) integer -> Observable.just(integer).delay(integer == 6 ? 500 : 0, TimeUnit.MILLISECONDS, Schedulers.trampoline())).subscribe(System.out::print);
        System.out.println();
        observable.flatMap(integer -> Observable.just(integer), true, 2, 2).subscribe(integer -> {
            System.out.println(System.currentTimeMillis() + "" + integer);
        });

        observable.flatMap(integer -> Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            emitter.onNext(200);
            emitter.onError(new Throwable("201"));
        }), throwable -> null, () -> null)
                .subscribe(integer -> System.out.println("flatMap:onNext," + integer),
                        throwable -> System.out.println("flatMap:onError," + throwable.getMessage()),
                        () -> System.out.println("flatMap:onComplete"));

        System.out.println();
        Observable.range(2, 5).concatMap(integer -> Observable.just(integer).repeat(integer - 1)).subscribe(System.out::print);
    }

    public static void flatMapIterable() {
        Observable.just("aaa", "bbb", "ccc")
                .flatMapIterable(Collections::singletonList).subscribe(System.out::println);
        Observable.just("ddd", "eee", "fff")
                .flatMapIterable(Collections::singletonList, (s, s2) -> {
                    return s.equals("eee") ? s : "";
                })
                .subscribe(System.out::println);
    }

    public static void buffer() {
        Observable<Integer> integerObservable = Observable.range(5, 5);
        integerObservable
                .buffer(2, 2)
                .subscribe(list -> {
                    for (Integer integer : list) {
                        System.out.print(integer);
                    }
                    System.out.println();
                });
        Observable.interval(2, TimeUnit.SECONDS, Schedulers.trampoline()).buffer(2, 2, TimeUnit.SECONDS)
                .subscribe(list -> {
                    for (long integer : list) {
                        System.out.print(integer);
                    }
                    System.out.println();
                });

    }

    public static void groupBy() {
        Observable<Integer> integerObservable = Observable.range(5, 5);
        integerObservable.groupBy(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                return integer % 2;
            }
        })
                .subscribe(integerIntegerGroupedObservable -> System.out.println(integerIntegerGroupedObservable.getKey()));
        Observable<GroupedObservable<Integer, Integer>> observable = Observable.concat(
                Observable.range(1, 4), Observable.range(1, 6)).groupBy(integer -> integer);
        Observable.concat(observable).subscribe(integer -> System.out.println("groupBy : " + integer));
        Observable<GroupedObservable<Integer, Student>> groupedObservable = Observable.fromIterable(studentList).groupBy(student -> student.getClassId());
        Observable.concat(groupedObservable).subscribe(student -> {
            System.out.println(student.toString());
        });
        groupedObservable.subscribe(integerStudentGroupedObservable -> {
            System.out.println(integerStudentGroupedObservable.getKey());
        });

    }

    public static void scan() {
        Observable<Integer> integerObservable = Observable.range(5, 5);
        integerObservable.scan((integer, integer2) -> integer + integer2).subscribe(System.out::println);
        integerObservable.scan(10, (i, i2) -> i + i2).subscribe(System.out::println);
    }

    public static void window() {
        Observable<Integer> integerObservable = Observable.range(5, 5);
        integerObservable.window(2, 2).subscribe(observable -> {
            System.out.println(observable);
            observable.subscribe(System.out::println);
        });
//        integerObservable.window(new Callable<ObservableSource<Integer>>() {
//            @Override
//            public ObservableSource<Integer> call() throws Exception {
//                return Observable.just(1);
//            }
//        }).subscribe(new Consumer<Observable<Integer>>() {
//            @Override
//            public void accept(Observable<Integer> integerObservable) throws Exception {
//
//            }
//        });
    }

    public static Observable<Integer> integerObservable = Observable.range(5, 5);

    public static void filter() {
        integerObservable.filter(integer -> integer % 2 == 0).subscribe(System.out::println);
    }

    public static void element() {
        integerObservable.elementAt(2, 4).subscribe(System.out::println);
        integerObservable.elementAtOrError(10)
                .subscribe(integer -> System.out.println("onNext:" + integer), throwable -> System.out.println("onError"));
        integerObservable.firstElement().subscribe(integer -> System.out.println("firstElement" + integer));
        integerObservable.lastElement().subscribe(integer -> System.out.println("lastElement" + integer));
        integerObservable.ignoreElements().subscribe(() -> System.out.println("ignoreElement"));
        //observable中数据要为单个，否则会报错
        integerObservable.singleElement().subscribe(integer -> System.out.println("singleElement" + integer));
    }

    public static void distinct() {
        Observable<Integer> observable = Observable.just(1, 2, 3, 3, 1);
        observable.distinct().subscribe(integer -> {
            System.out.println("distinct:" + integer);
        });
        //distinct里面带有function是用来过滤重复后进行处理然后生成新的Observable将数据往下传
        observable.distinct(integer -> integer)
                .subscribe(System.out::println);
        //只有相邻的重复才会去重
        observable.distinctUntilChanged()
                .subscribe(integer -> System.out.println("distinctUntilChange:" + integer));
    }

    private static Consumer<Object> printlnConsumer = o -> System.out.println(o);

    private static Consumer<Object> getCustomerConsumer(String tag) {
        return o -> System.out.println(tag + ":" + o);
    }

    public static void skip() {
        integerObservable.skip(1).subscribe(getCustomerConsumer("skip"));
        integerObservable.skipLast(2).subscribe(getCustomerConsumer("skipLast"));
//        integerObservable.skipUntil(observer -> observer.onComplete()).subscribe(getCustomerConsumer("skipUntil"));
        Observable.intervalRange(1, 5, 0, 1, TimeUnit.SECONDS, Schedulers.trampoline())
                .skipUntil(Observable.intervalRange(6, 5, 3, 1, TimeUnit.SECONDS))
                .subscribe(getCustomerConsumer("skipUntil"));
//        Observable.just(4, 5, 6, 7, 8).skipWhile(integer -> integer > 6).subscribe(getCustomerConsumer("skipWhile"));
        //上面方法不生效？？？
        Observable.just(4, 5, 6, 7, 8).skipWhile(integer -> integer < 6).subscribe(getCustomerConsumer("skipWhile"));
    }

    /**
     * take2:5
     * take2:6
     * takeLast:8
     * takeLast:9
     * takeUntil:1
     * takeUntil:2
     * takeUntil:3
     * takeWhile:5
     */
    public static void take() {
        integerObservable.take(2).subscribe(getCustomerConsumer("take2"));
        integerObservable.takeLast(2).subscribe(getCustomerConsumer("takeLast"));
        Observable.intervalRange(1, 5, 0, 1, TimeUnit.SECONDS, Schedulers.trampoline())
                .takeUntil(Observable.intervalRange(6, 5, 3, 1, TimeUnit.SECONDS))
                .subscribe(getCustomerConsumer("takeUntil"));
        integerObservable.takeWhile(integer -> integer < 6).subscribe(getCustomerConsumer("takeWhile"));
    }

    public static void throttle() {
        Observable<Long> timeObservable = Observable.interval(500, TimeUnit.MILLISECONDS, Schedulers.trampoline());
//        timeObservable.throttleFirst(300, TimeUnit.MILLISECONDS)
//                .subscribe(getCustomerConsumer("throttleFirst300"));
//        timeObservable.throttleFirst(500, TimeUnit.MILLISECONDS)
//                .subscribe(getCustomerConsumer("throttleFirst500"));
//        timeObservable.throttleFirst(800, TimeUnit.MILLISECONDS)
//                .subscribe(getCustomerConsumer("throttleFirst800"));
//        timeObservable.throttleFirst(1000, TimeUnit.MILLISECONDS)
//                .subscribe(getCustomerConsumer("throttleFirst1000"));
//        timeObservable.throttleLast(800, TimeUnit.MILLISECONDS)
//                .subscribe(getCustomerConsumer("throttleLast300"));
//        timeObservable.throttleLatest(1300, TimeUnit.MILLISECONDS)
//                .subscribe(getCustomerConsumer("throttleLatest"));
        timeObservable.throttleWithTimeout(400,TimeUnit.MILLISECONDS)
                .subscribe(getCustomerConsumer("throttleWithTimeout"));
    }

    public static void debounce(){

    }
}
