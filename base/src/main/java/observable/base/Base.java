package observable.base;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
        scan();
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
    }
}
