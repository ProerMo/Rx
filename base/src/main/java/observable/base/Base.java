package observable.base;

import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import observable.base.bean.Student;
import org.reactivestreams.Publisher;

import javax.xml.ws.http.HTTPException;
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
//        throttle();
//        debounce();
//        sample();
//        startWith();
//        merge();
//        concat();
//        zip();
//        combineLatest();
//        delay();
//        doSth();
//        timeout();
//        onErrorHandle();
//        retry();
//        allAny();
//        containsIsEmpty();
//        sequenceEqual();
//        amb();
//        defaultIfEmpty();
//        toList();
//        toMap();
//        to();
//        backpressure();
        backpressure1();
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
        List<Integer> integerList = new ArrayList<>();
        integerList.add(2);
        integerList.add(4);
        integerList.add(6);
        integerList.add(8);
        integerList.add(10);
        Observable.fromIterable(integerList)
                .buffer(2, 2)
                .subscribe(list -> {
                    System.out.println(list.toString() + list.get(0) + "---" + list.size());
                });
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
        timeObservable.throttleWithTimeout(400, TimeUnit.MILLISECONDS)
                .subscribe(getCustomerConsumer("throttleWithTimeout"));
    }

    public static Observable<String> timeObservable = Observable.create(emitter -> {
        emitter.onNext("1");
        Thread.sleep(300);
        emitter.onNext("2");
        Thread.sleep(700);
        emitter.onNext("3");
        Thread.sleep(500);
        emitter.onNext("4");
        Thread.sleep(200);
        emitter.onComplete();
    });

    /**
     * hello world
     * debounce:2
     * debounce:3
     * debounce:4
     * 两次发射间隔小于timeout则取后面一次
     */
    public static void debounce() {
        timeObservable.debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(getCustomerConsumer("debounce"));
    }

    /**
     * hello world
     * sample:2
     * sample:3
     * sample:4
     * 同上
     */
    public static void sample() {
//        debounce();
        timeObservable.sample(400, TimeUnit.MILLISECONDS)
                .subscribe(getCustomerConsumer("sample"));
    }

    public static void startWith() {
        //在前面插入一个2
        integerObservable.startWith(2)
                .subscribe(getCustomerConsumer("startWith"));
        List<Integer> i = new ArrayList<Integer>();
        i.add(11);
        i.add(22);
        i.add(33);
        i.add(44);
        //将list中的插入到前面
        integerObservable.startWith(i).subscribe(getCustomerConsumer("startWith"));
        //将1,3,4插入到前面
        integerObservable.startWithArray(1, 3, 4).subscribe(getCustomerConsumer("startWithArray"));
    }

    /**
     * hello world
     * merge:123
     * merge:321
     * merge:111
     * merge:333
     */
    public static void merge() {
        Observable.merge(timeObservable, timeObservable).subscribe(getCustomerConsumer("merge"));
        Observable.merge(
                Observable.interval(1, TimeUnit.SECONDS, Schedulers.trampoline()).map(new Function<Long, String>() {
                    @Override
                    public String apply(Long aLong) throws Exception {
                        return "A" + aLong;
                    }
                }),
                Observable.interval(1, TimeUnit.SECONDS, Schedulers.trampoline()).map(new Function<Long, String>() {
                    @Override
                    public String apply(Long aLong) throws Exception {
                        return "B" + aLong;
                    }
                }))
                .subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(System.out::println);
    }

    //merge跟concat类似都是将多个发射源合并发射，区别是concat是按照发射源顺序发射，发完一个再到另外一个，而merge不是。
    public static void concat() {
        Observable.concat(Observable.create(emitter -> {
            emitter.onNext(123);
            Thread.sleep(2000);
            emitter.onNext(321);
            emitter.onComplete();
        }), Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            emitter.onNext(111);
            emitter.onNext(333);
            emitter.onComplete();
        })).subscribe(getCustomerConsumer("concat"));
    }

    //将两个发射源的数据自定义规则进行处理然后输入，输出的数量以发射源中最少的个数
    public static void zip() {
        Observable<Integer> observable = Observable.range(5, 4);
        Observable.zip(integerObservable, observable, (integer, integer2) -> String.valueOf(integer * integer2)).subscribe(System.out::println);
        Observable<Long> observableTime = Observable.interval(1, TimeUnit.SECONDS, Schedulers.trampoline());
        Observable.zip(timeObservable, Observable.create(emitter -> {
            emitter.onNext("1");
            Thread.sleep(300);
            emitter.onNext("2");
            Thread.sleep(200);
            emitter.onNext("3");
            Thread.sleep(200);
            emitter.onNext("4");
            Thread.sleep(200);
            emitter.onComplete();
        }), (s, s2) -> s + s2).subscribe(System.out::println);
    }

    /**
     * a4
     * b4
     * c4
     * d4
     * <p>
     * 1d
     * 2d
     * 3d
     * 4d
     * https://blog.csdn.net/jdsjlzx/article/details/53040293
     * Android用RxJava combineLatest操作符处理复杂表单验证问题
     */
    public static void combineLatest() {
        Observable.combineLatest(Observable.range(1, 6), Observable.range(6, 7), (i, i1) -> i * i1).subscribe(System.out::println);
        Observable.combineLatest(Observable.just(1, 2, 3), Observable.just(1, 2, 3), (u, u1) -> u + "" + u1).subscribe(System.out::println);
        Observable.combineLatest(Observable.create(emitter -> {
            emitter.onNext("a");
            Thread.sleep(300);
            emitter.onNext("b");
            Thread.sleep(2000);
            emitter.onNext("c");
            Thread.sleep(200);
            emitter.onNext("d");
            Thread.sleep(200);
            emitter.onComplete();
        }), timeObservable, (i, u) -> u + i).subscribe(System.out::println);

    }

    public static void delay() {
        Observable.range(5, 5).delay(2, TimeUnit.SECONDS)
                .subscribe(getCustomerConsumer("delay"));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void doSth() {
        Observable.range(1, 5)
                .doOnEach(integerNotification -> System.out.println("Each : " + integerNotification.getValue()))
                .doOnComplete(() -> System.out.println("complete"))
                .doFinally(() -> System.out.println("finally"))
                .doAfterNext(i -> System.out.println("after next : " + i))
                .doOnSubscribe(disposable -> System.out.println("subscribe"))
                .doOnTerminate(() -> System.out.println("terminal"))
                .subscribe(i -> System.out.println("subscribe : " + i));
    }

    public static void timeout() {
        Observable.interval(1000, 200, TimeUnit.MILLISECONDS, Schedulers.trampoline())
                .timeout(500, TimeUnit.MILLISECONDS, Observable.rangeLong(1, 5))
                .subscribe(getCustomerConsumer("timeout"));

    }

    public static void onErrorHandle() {
        HTTPException exception = new HTTPException(404);
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onError(exception);
            emitter.onNext(123);
        });
        observable.onErrorReturn(throwable -> 111).subscribe(getCustomerConsumer("onErrorReturn"));
        observable.onErrorResumeNext(Observable.range(1, 5)).subscribe(getCustomerConsumer("onErrorResumeNext"));
        observable.onErrorReturnItem(234).subscribe(getCustomerConsumer("onErrorReturnItem"));
        observable.onErrorResumeNext(throwable -> {
            return Observable.create(emitter -> emitter.onNext(((HTTPException) throwable).getStatusCode()));
        }).subscribe(getCustomerConsumer("onErrorResumeNext"));

    }

    public static void retry() {
        Observable.create(((ObservableOnSubscribe<Integer>) emitter -> {
            emitter.onNext(0);
            emitter.onError(new Throwable("Error1"));
            emitter.onError(new Throwable("Error2"));
        })).retry(2).subscribe(i -> System.out.println("onNext : " + i), error -> System.out.print("onError : " + error));
    }

    public static void allAny() {
        Observable.range(5, 5).all(i -> i > 5).subscribe(System.out::println); // false
        Observable.range(5, 5).any(i -> i > 5).subscribe(System.out::println); // true
    }

    public static void containsIsEmpty() {
        Observable.range(5, 5).contains(4).subscribe(System.out::println); // false
        Observable.range(5, 5).isEmpty().subscribe(System.out::println); // false
        Observable.create(emitter -> {
            emitter.onNext("");
        }).isEmpty().subscribe(getCustomerConsumer("isEmpty"));
        Observable.empty().isEmpty().subscribe(getCustomerConsumer("isEmpty"));
    }

    public static void sequenceEqual() {
        Observable.sequenceEqual(Observable.range(1, 5), Observable.range(1, 5)).subscribe(System.out::println);
    }

    public static void amb() {
        Observable.amb(Arrays.asList(Observable.range(1, 5), Observable.range(6, 5))).subscribe(System.out::print);
    }

    public static void defaultIfEmpty() {
        Observable.create((ObservableOnSubscribe<Integer>) Emitter::onComplete).defaultIfEmpty(6).subscribe(System.out::print);
    }

    public static void toList() {
        Observable.range(1, 5).toList().subscribe(System.out::println);
        Observable.range(1, 5).toSortedList(Comparator.comparingInt(o -> -o)).subscribe(System.out::println);
    }

    //转换成map
    public static void toMap() {
        Observable.range(8, 10).toMap(integer -> integer + 1 + "").subscribe(getCustomerConsumer("toMap"));
        Observable.range(8, 10).map(integer -> Integer.toHexString(integer)).subscribe(getCustomerConsumer("map"));
        Observable.range(8, 10).toMap(integer -> Integer.toHexString(integer), integer -> integer).subscribe(getCustomerConsumer("toMap"));
    }

    public static void to() {
        Observable.range(5, 5).to(integerObservable -> integerObservable.map(integer -> integer + "")).subscribe(getCustomerConsumer("to"));
    }

    private static void compute(int i) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("computing : " + i);
    }

    private static void backpressure() {
        Flowable.range(1, 1000).observeOn(Schedulers.computation()).subscribe(integer -> compute(integer));

        try {
            Thread.sleep(500 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //.MissingBackpressureException: Could not emit value due to lack of requests
    private static void backpressure1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        source.observeOn(Schedulers.computation()).subscribe(v -> compute(v), Throwable::printStackTrace);
        for (int i = 0; i < 1_000_000; i++) source.onNext(i);
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
