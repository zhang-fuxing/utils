package com.zhangfuxing.tools.switchs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.*;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/11/28
 * @email zhangfuxing1010@163.com
 */
public class SwitchUtil {

    public static SwitchRun run() {
        return new SwitchRun();
    }

    public static <T, R> SwitchMap<T, R> map(T input, Class<R> outputType) {
        return new SwitchMap<T, R>(input);
    }

    public static <T, U, R> SwitchBiMap<T, U, R> map(T input0, U input1, Class<R> outputType) {
        return new SwitchBiMap<>(input0, input1);
    }

    public static <T> SwitchAccept<T> accept(T input) {
        return new SwitchAccept<>(input);
    }

    public static <T> SwitchGet<T> get(Class<T> outputType) {
        return new SwitchGet<>();
    }

    public static class SwitchRun {
        private final List<SwitchModel<Runnable>> runnableModels = new CopyOnWriteArrayList<>();
        private boolean matchAll = false;

        public SwitchRun matchAll() {
            matchAll = true;
            return this;
        }

        public SwitchRun add(boolean occasion, Runnable run) {
            runnableModels.add(new SwitchModel<>(occasion, run));
            return this;
        }

        public SwitchRun add(int serial, boolean occasion, Runnable run) {
            runnableModels.add(new SwitchModel<>(serial, occasion, run));
            return this;
        }

        public void exec() {
            runnableModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (SwitchModel<Runnable> model : runnableModels) {
                if (model.occasion) {
                    model.content.run();
                    if (!matchAll) {
                        return;
                    }
                }
            }
        }

        public void submit() {
            runnableModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (var funcModel : runnableModels) {
                if (!funcModel.occasion) {
                    continue;
                }
                funcModel.content.run();
                break;
            }
        }
    }

    public static class SwitchMap<T, R> {
        private final List<SwitchModel<Function<T, R>>> funcModels = new CopyOnWriteArrayList<>();
        private boolean matchAll = false;
        private T input;

        private SwitchMap() {
        }

        private SwitchMap(T input) {
            this.input = input;
        }

        public SwitchMap<T, R> add(boolean occasion, Function<T, R> map) {
            funcModels.add(new SwitchModel<>(occasion, map));
            return this;
        }

        public SwitchMap<T, R> add(int serial, boolean occasion, Function<T, R> map) {
            funcModels.add(new SwitchModel<>(serial, occasion, map));
            return this;
        }

        public SwitchMap<T, R> add(Predicate<T> predicate, Function<T, R> map) {
            return this.add(predicate.test(input), map);
        }

        public SwitchMap<T, R> add(int serial, Predicate<T> predicate, Function<T, R> map) {
            funcModels.add(new SwitchModel<>(serial, predicate.test(input), map));
            return this;
        }

        public SwitchMap<T, R> matchAll() {
            matchAll = true;
            return this;
        }

        public List<R> exec() {
            List<R> result = new ArrayList<>();
            funcModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (SwitchModel<Function<T, R>> funcModel : funcModels) {
                if (!funcModel.occasion) {
                    continue;
                }
                R apply = funcModel.content.apply(input);
                result.add(apply);
                if (!matchAll) {
                    break;
                }
            }

            return result;
        }

        public R submit() {
            funcModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (SwitchModel<Function<T, R>> funcModel : funcModels) {
                if (!funcModel.occasion) {
                    continue;
                }
                return funcModel.content.apply(input);
            }
            return null;
        }

    }

    public static class SwitchBiMap<T, U, R> {
        private final List<SwitchModel<BiFunction<T, U, R>>> biFuncModels = new CopyOnWriteArrayList<>();
        private boolean matchAll = false;
        private T input0;
        private U input1;

        private SwitchBiMap() {
        }

        private SwitchBiMap(T input0, U input1) {
            this.input0 = input0;
            this.input1 = input1;
        }

        public SwitchBiMap<T, U, R> matchAll() {
            matchAll = true;
            return this;
        }

        public SwitchBiMap<T, U, R> add(boolean occasion, BiFunction<T, U, R> biMap) {
            biFuncModels.add(new SwitchModel<>(occasion, biMap));
            return this;
        }

        public SwitchBiMap<T, U, R> add(int serial, boolean occasion, BiFunction<T, U, R> biMap) {
            biFuncModels.add(new SwitchModel<>(serial, occasion, biMap));
            return this;
        }

        public SwitchBiMap<T, U, R> add(BiPredicate<T, U> biPredicate, BiFunction<T, U, R> biMap) {
            return this.add(biPredicate.test(input0, input1), biMap);
        }

        public SwitchBiMap<T, U, R> add(int serial, BiPredicate<T, U> biPredicate, BiFunction<T, U, R> biMap) {
            biFuncModels.add(new SwitchModel<>(serial, biPredicate.test(input0, input1), biMap));
            return this;
        }

        public List<R> exec() {
            List<R> result = new ArrayList<>();
            biFuncModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (SwitchModel<BiFunction<T, U, R>> biFuncModel : biFuncModels) {
                if (!biFuncModel.occasion) {
                    continue;
                }
                R value = biFuncModel.content.apply(input0, input1);
                result.add(value);
                if (!matchAll) {
                    break;
                }
            }
            return result;
        }

        public R submit() {
            biFuncModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (var funcModel : biFuncModels) {
                if (!funcModel.occasion) {
                    continue;
                }
                return funcModel.content.apply(input0, input1);
            }
            return null;
        }
    }

    public static class SwitchAccept<T> {
        private final List<SwitchModel<Consumer<T>>> consumerModels = new CopyOnWriteArrayList<>();
        private boolean matchAll = false;
        private T input;

        private SwitchAccept() {
        }

        private SwitchAccept(T input) {
            this.input = input;
        }

        public SwitchAccept<T> matchAll() {
            matchAll = true;
            return this;
        }

        public SwitchAccept<T> add(boolean occasion, Consumer<T> consumer) {
            consumerModels.add(new SwitchModel<>(occasion, consumer));
            return this;
        }

        public SwitchAccept<T> add(int serial, boolean occasion, Consumer<T> consumer) {
            consumerModels.add(new SwitchModel<>(serial, occasion, consumer));
            return this;
        }

        public SwitchAccept<T> add(Predicate<T> predicate, Consumer<T> consumer) {
            return this.add(predicate.test(input), consumer);
        }

        public SwitchAccept<T> add(int serial, Predicate<T> predicate, Consumer<T> consumer) {
            consumerModels.add(new SwitchModel<>(serial, predicate.test(input), consumer));
            return this;
        }

        public void exec() {
            consumerModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (SwitchModel<Consumer<T>> model : consumerModels) {
                if (!model.occasion) {
                    continue;
                }
                model.content.accept(input);
                if (!matchAll) {
                    break;
                }
            }
        }

        public void submit() {
            consumerModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (var funcModel : consumerModels) {
                if (!funcModel.occasion) {
                    continue;
                }
                funcModel.content.accept(input);
                break;
            }
        }
    }

    public static class SwitchGet<T> {
        private final List<SwitchModel<Supplier<T>>> supplierModels = new CopyOnWriteArrayList<>();
        private boolean matchAll = false;

        public SwitchGet<T> matchAll() {
            matchAll = true;
            return this;
        }

        public SwitchGet<T> add(boolean occasion, Supplier<T> get) {
            supplierModels.add(new SwitchModel<>(occasion, get));
            return this;
        }

        public SwitchGet<T> add(int serial, boolean occasion, Supplier<T> get) {
            supplierModels.add(new SwitchModel<>(serial, occasion, get));
            return this;
        }

        public List<T> exec() {
            List<T> result = new ArrayList<>();
            supplierModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (SwitchModel<Supplier<T>> model : supplierModels) {
                if (model.occasion) {
                    T value = model.content.get();
                    result.add(value);
                    if (!matchAll) {
                        break;
                    }
                }
            }
            return result;
        }

        public T submit() {
            supplierModels.sort(Comparator.comparing(SwitchModel::getSerial));
            for (SwitchModel<Supplier<T>> model : supplierModels) {
                if (model.occasion) {
                    return model.content.get();
                }
            }
            return null;
        }
    }

    private static class SwitchModel<T> {
        private int serial = Integer.MAX_VALUE;
        private boolean occasion;
        private T content;

        public SwitchModel() {
        }

        public SwitchModel(boolean occasion, T content) {
            this.occasion = occasion;
            this.content = content;
        }

        public SwitchModel(int serial, boolean occasion, T content) {
            this.serial = serial;
            this.occasion = occasion;
            this.content = content;
        }

        public int getSerial() {
            return serial;
        }

        public void setSerial(int serial) {
            this.serial = serial;
        }

        public boolean isOccasion() {
            return occasion;
        }

        public void setOccasion(boolean occasion) {
            this.occasion = occasion;
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }

    }

}
