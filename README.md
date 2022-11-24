# Android object canvas

## 基本介绍

这个库以面向对象的方式来操作Android上的原生canvas绘图,我也不懂android开发,一些思路仿照web方面开发实现的, 元素实现类似html div的盒子模型, 事件分发机制也借鉴了js的dom事件模型先捕获再冒泡, 定时器借鉴了js的方式, 动画实现借鉴了animate.js

* 支持简单绝对布局(通过left,top属性来确定位置)
* 支持Matrix变换
* 支持滚动条
* 支持事件分发
* 支持元素盒子模型
* 支持缓动动画