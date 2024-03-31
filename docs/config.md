# 配置文件

每个工作区都有自己独立的配置文件，文件名为`WindonlyConfig.config`。

配置文件中的所有配置项都可以删除，当配置项不存在时，则会使用默认值。重新在配置文件中新增配置项即可重新生效。

## 配置内容

```json
{
  "fontSize" : 16.0,
  "buttonSize" : 16.0,
  "imageSize" : 16.0,
  "magnification" : 1.0,
  "displayImageMaxSize" : 1024000,
  "displayFileNumber" : 15,
  "alwaysShow" : true,
  "lock" : false,
  "slide" : true
}
```

## 配置项说明

|         配置名         |     默认值     | 说明                                          |
|:-------------------:|:-----------:|:--------------------------------------------|
|      fontSize       |    16.0     | 字体大小                                        |
|     buttonSize      |    16.0     | 按钮大小                                        |
|      imageSize      |    16.0     | 列表中图片缩略图尺寸                                  |
|    magnification    |     1.0     | 字体大小缩放比例                                    |
| displayImageMaxSize | 1024 * 1024 | 列表中显示图片缩略图的最大文件大小（单位byte）；0 - 都不显示；-1 - 都显示 |
|  displayFileNumber  |     10      | 列表中多文件的省略阈值，超出数量的文件信息会被隐藏；-1 - 不隐藏          |
|     alwaysShow      |    true     | 是否在前台置顶；true - 置顶；false - 不置顶               |
|        lock         |    false    | 工作区锁定；true - 锁定；false - 不锁定                 |
|        slide        |    false    | 面板贴边；true - 贴边；false - 取消贴边                 |

## 注意

`displayImageMaxSize`多大可能会导致面板在渲染多个图片项时卡顿。