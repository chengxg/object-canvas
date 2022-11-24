package com.github.chengxg.object_canvas;

/**
 *
 * 缓动函数库
 * 取自https://github.com/ai/easings.net/blob/master/src/easings/easingsFunctions.ts
 * 这些缓动函数都是直角坐标系中运行时间与位移的函数图
 * x轴为运行时间所占总时间的比例, 取值范围[0-1]
 * 返回的值是位移的变化比例, 范围也是[0-1]
 * 函数曲线见:https://easings.net/ https://www.xuanfengge.com/easeing/easeing/
 */

import java.util.HashMap;
import java.util.Map;

public class AnimeEasings {
    private static final double c1 = 1.70158;
    private static final double c2 = 2.59491;   // c1 * 1.525
    private static final double c3 = 2.70158;   // c1 + 1
    private static final double c4 = 2.094395;  //(2 * PI) / 3
    private static final double c5 = 1.396263;  //(2 * PI) / 4.5

    private static final double n1 = 7.5625;
    private static final double d1 = 2.75;
    private static final double PI = Math.PI;

    public static Map<String, Anime.Easing> easingMap = new HashMap<>();

    static {
        easingMap.put("linear", AnimeEasings::linear);
        easingMap.put("easeInQuad", AnimeEasings::easeInQuad);
        easingMap.put("easeOutQuad", AnimeEasings::easeOutQuad);
        easingMap.put("easeInOutQuad", AnimeEasings::easeInOutQuad);
        easingMap.put("easeInCubic", AnimeEasings::easeInCubic);
        easingMap.put("easeOutCubic", AnimeEasings::easeOutCubic);
        easingMap.put("easeInOutCubic", AnimeEasings::easeInOutCubic);
        easingMap.put("easeInQuart", AnimeEasings::easeInQuart);
        easingMap.put("easeOutQuart", AnimeEasings::easeOutQuart);
        easingMap.put("easeInOutQuart", AnimeEasings::easeInOutQuart);
        easingMap.put("easeInQuint", AnimeEasings::easeInQuint);
        easingMap.put("easeOutQuint", AnimeEasings::easeOutQuint);
        easingMap.put("easeInOutQuint", AnimeEasings::easeInOutQuint);
        easingMap.put("easeInSine", AnimeEasings::easeInSine);
        easingMap.put("easeOutSine", AnimeEasings::easeOutSine);
        easingMap.put("easeInOutSine", AnimeEasings::easeInOutSine);
        easingMap.put("easeInExpo", AnimeEasings::easeInExpo);
        easingMap.put("easeOutExpo", AnimeEasings::easeOutExpo);
        easingMap.put("easeInOutExpo", AnimeEasings::easeInOutExpo);
        easingMap.put("easeInCirc", AnimeEasings::easeInCirc);
        easingMap.put("easeOutCirc", AnimeEasings::easeOutCirc);
        easingMap.put("easeInOutCirc", AnimeEasings::easeInOutCirc);
        easingMap.put("easeInBack", AnimeEasings::easeInBack);
        easingMap.put("easeOutBack", AnimeEasings::easeOutBack);
        easingMap.put("easeInOutBack", AnimeEasings::easeInOutBack);
        easingMap.put("easeInElastic", AnimeEasings::easeInElastic);
        easingMap.put("easeOutElastic", AnimeEasings::easeOutElastic);
        easingMap.put("easeInOutElastic", AnimeEasings::easeInOutElastic);
        easingMap.put("easeInBounce", AnimeEasings::easeInBounce);
        easingMap.put("easeOutBounce", AnimeEasings::easeOutBounce);
        easingMap.put("easeInOutBounce", AnimeEasings::easeInOutBounce);
    }

    public static Anime.Easing getEasing(String name) {
        return easingMap.get(name);
    }

    private static double cos(double v) {
        return Math.cos(v);
    }

    private static double sin(double v) {
        return Math.sin(v);
    }

    private static double abs(double v) {
        return Math.abs(v);
    }

    private static double sqrt(double v) {
        return Math.sqrt(v);
    }

    private static double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    //基础方法
    public static double bounceOut(double x) {
        // const n1 = 7.5625;
        // const d1 = 2.75;

        // if(x < 1 / d1) {
        //   return n1 * x * x;
        // } else if(x < 2 / d1) {
        //   return n1 * (x -= 1.5 / d1) * x + 0.75;
        // } else if(x < 2.5 / d1) {
        //   return n1 * (x -= 2.25 / d1) * x + 0.9375;
        // } else {
        //   return n1 * (x -= 2.625 / d1) * x + 0.984375;
        // }

        if (x < 0.363636) {
            return n1 * x * x;
        } else if (x < 0.727273) {
            return n1 * (x -= 0.545455) * x + 0.75;
        } else if (x < 0.909091) {
            return n1 * (x -= 0.818182) * x + 0.9375;
        } else {
            return n1 * (x -= 0.954545) * x + 0.984375;
        }
    }

    public static double linear(double x) {
        return x;
    }

    public static double easeInQuad(double x) {
        return x * x;
    }

    public static double easeOutQuad(double x) {
        // return 1 - (1 - x) * (1 - x);
        return 1.0 - (1.0 - x) * (1.0 - x);
    }

    public static double easeInOutQuad(double x) {
        // return x < 0.5 ? 2 * x * x : 1 - pow(-2 * x + 2, 2) / 2;
        if (x < 0.5) {
            return 2.0 * x * x;
        }
        double temp = -2.0 * x + 2.0;
        return 1.0 - temp * temp / 2.0;
    }

    public static double easeInCubic(double x) {
        return x * x * x;
    }

    public static double easeOutCubic(double x) {
        // return 1.0 - pow(1.0 - x, 3);
        double temp = 1.0 - x;
        return 1.0 - temp * temp * temp;
    }

    public static double easeInOutCubic(double x) {
        // return x < 0.5 ? 4 * x * x * x : 1 - pow(-2 * x + 2, 3) / 2;
        if (x < 0.5) {
            return 4.0 * x * x * x;
        }
        double temp = -2.0 * x + 2.0;
        return 1 - temp * temp * temp / 2.0;
    }

    public static double easeInQuart(double x) {
        return x * x * x * x;
    }

    public static double easeOutQuart(double x) {
        // return 1 - pow(1 - x, 4);
        double temp = 1.0 - x;
        return 1.0 - temp * temp * temp * temp;
    }

    public static double easeInOutQuart(double x) {
        // return x < 0.5 ? 8 * x * x * x * x : 1 - pow(-2 * x + 2, 4) / 2;
        if (x < 0.5) {
            return 8.0 * x * x * x * x;
        }
        double temp = -2.0 * x + 2.0;
        return 1 - temp * temp * temp * temp / 2;
    }

    public static double easeInQuint(double x) {
        return x * x * x * x * x;
    }

    public static double easeOutQuint(double x) {
        // return 1 - pow(1 - x, 5);
        double temp = 1.0 - x;
        return 1.0 - temp * temp * temp * temp * temp;
    }

    public static double easeInOutQuint(double x) {
        // return x < 0.5 ? 16 * x * x * x * x * x : 1 - pow(-2 * x + 2, 5) / 2;
        if (x < 0.5) {
            return 16.0 * x * x * x * x * x;
        }
        double temp = -2.0 * x + 2.0;
        return 1.0 - temp * temp * temp * temp * temp / 2.0;
    }

    public static double easeInSine(double x) {
        return 1.0 - cos((x * PI) / 2.0);
    }

    public static double easeOutSine(double x) {
        return sin((x * PI) / 2.0);
    }

    public static double easeInOutSine(double x) {
        return -(cos(PI * x) - 1.0) / 2.0;
    }

    public static double easeInExpo(double x) {
        // return x == 0 ? 0 : pow(2, 10 * x - 10);
        if (abs(x) < 0.001) {
            return 0;
        }
        return pow(2.0, 10.0 * x - 10.0);
    }

    public static double easeOutExpo(double x) {
        // return x == 1 ? 1 : 1 - pow(2, -10 * x);
        if (abs(x - 1.0) < 0.001) {
            return 1;
        }
        return 1.0 - pow(2, -10.0 * x);
    }

    public static double easeInOutExpo(double x) {
        // return x == 0
        //          ? 0
        //          : x == 1
        //              ? 1
        //              : x < 0.5
        //                  ? pow(2, 20 * x - 10) / 2
        //                  : (2 - pow(2, -20 * x + 10)) / 2;
        if (abs(x) < 0.001) {
            return 0;
        }
        if (abs(x - 1.0) < 0.001) {
            return 1;
        }
        if (x < 0.5) {
            return pow(2.0, 20.0 * x - 10.0) / 2.0;
        }
        return (2.0 - pow(2.0, -20.0 * x + 10.0)) / 2.0;
    }

    public static double easeInCirc(double x) {
        // return 1 - sqrt(1 - pow(x, 2));
        return 1.0 - sqrt(1.0 - x * x);
    }

    public static double easeOutCirc(double x) {
        // return sqrt(1 - pow(x - 1, 2));
        double temp = x - 1.0;
        return sqrt(1.0 - temp * temp);
    }

    public static double easeInOutCirc(double x) {
        // return x < 0.5
        //          ? (1 - sqrt(1 - pow(2 * x, 2))) / 2
        //          : (sqrt(1 - pow(-2 * x + 2, 2)) + 1) / 2;
        if (x < 0.5) {
            return (1.0 - sqrt(1.0 - 4.0 * x * x)) / 2.0;
        }
        double temp = -2.0 * x + 2.0;
        return (sqrt(1.0 - temp * temp) + 1.0) / 2.0;
    }

    public static double easeInBack(double x) {
        return c3 * x * x * x - c1 * x * x;
    }

    public static double easeOutBack(double x) {
        // return 1 + c3 * pow(x - 1, 3) + c1 * pow(x - 1, 2);
        double temp = x - 1.0;
        return 1.0 + c3 * temp * temp * temp + c1 * temp * temp;
    }

    public static double easeInOutBack(double x) {
        // return x < 0.5
        //          ? (pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
        //          : (pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
        if (x < 0.5) {
            return (4.0 * x * x * ((c2 + 1.0) * 2.0 * x - c2)) / 2.0;
        }
        double temp = 2.0 * x - 2.0;
        return (temp * temp * ((c2 + 1.0) * temp + c2) + 2.0) / 2.0;
    }

    public static double easeInElastic(double x) {
        // return x == 0
        //          ? 0
        //          : x == 1
        //              ? 1
        //              : -pow(2, 10 * x - 10) * sin((x * 10 - 10.75) * c4);
        if (abs(x) < 0.0001) {
            return 0;
        }
        if (abs(x - 1.0) < 0.0001) {
            return 1;
        }
        return -pow(2.0, 10.0 * x - 10.0) * sin((x * 10.0 - 10.75) * c4);
    }

    public static double easeOutElastic(double x) {
        // return x == 0
        //          ? 0
        //          : x == 1
        //              ? 1
        //              : pow(2, -10 * x) * sin((x * 10 - 0.75) * c4) + 1;
        if (abs(x) < 0.0001) {
            return 0;
        }
        if (abs(x - 1.0) < 0.0001) {
            return 1;
        }
        return pow(2.0, -10.0 * x) * sin((x * 10.0 - 0.75) * c4) + 1.0;
    }

    public static double easeInOutElastic(double x) {
        // return x == 0
        //          ? 0
        //          : x == 1
        //              ? 1
        //              : x < 0.5
        //                  ? -(pow(2, 20 * x - 10) * sin((20 * x - 11.125) * c5)) / 2
        //                  : (pow(2, -20 * x + 10) * sin((20 * x - 11.125) * c5)) / 2 + 1;
        if (abs(x) < 0.0001) {
            return 0;
        }
        if (abs(x - 1.0) < 0.0001) {
            return 1;
        }
        if (x < 0.5) {
            return -(pow(2.0, 20.0 * x - 10.0) * sin((20.0 * x - 11.125) * c5)) / 2.0;
        }
        return (pow(2.0, -20.0 * x + 10.0) * sin((20.0 * x - 11.125) * c5)) / 2.0 + 1.0;
    }

    public static double easeInBounce(double x) {
        return 1.0 - bounceOut(1.0 - x);
    }

    public static double easeOutBounce(double x) {
        return bounceOut(x);
    }

    public static double easeInOutBounce(double x) {
        return x < 0.5
                ? (1 - bounceOut(1 - 2.0 * x)) / 2.0
                : (1 + bounceOut(2.0 * x - 1)) / 2.0;
    }

}
