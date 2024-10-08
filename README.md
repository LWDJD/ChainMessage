# 欢迎使用ChainMessage

* [ ] [CainMessage](https://github.com/LWDJD/ChainMessage)，是一款基于 JAVA，为实现在区块链聊天的软件；
* [ ] 内置查询交易信息，16进制Input Data自动转换为文本（UTF8）等功能。
* [ ] 具有账户和聊天的管理功能
* [ ] 可以与其他人在相同 地址的频道 聊天交流
* [ ] [教程视频](https://www.bilibili.com/video/BV14Bs5e6E3q/)
---

## 如何使用


* [ ] 打开后在左上角有选项；
* [ ] 在账户选项中导入或创建一个账户（仅支持OKTC-test链）；
* [ ] 在聊天选项中添加一个聊天地址（仅支持频道模式）；
* [ ] 在设置选项中选择X-API-KEY,在里面添加Key（下面有获取教程）
* [ ] 回到主界面在左侧选择一个聊天；
* [ ] 输入刚才设置的密码并确认；
* [ ] 已经可以查看消息了（如果消息多会需要多加载一会）；
* [ ] 刚开始最多加载最近的100条；
* [ ] 过一段时间再刷新一下就会把已经加载好的数据全部刷出来（API限制最多查询最近的一万条交易）；
* [ ] 接下来是发消息的教程；
* [ ] 在左下角选择刚才创建的账户；
* [ ] 点右侧的解锁账户，输入密码并确认；
* [ ] 在文本框内输入你要发送的消息然后点发送（需要有OKT测试代币作为发送费用）；
* [ ] 会弹出交易的提示信息；

---

## 如何获取`OKT`测试代币

* [ ] 在[OKTC 测试网 水龙头](https://www.okx.com/zh-hans/oktc/faucet)领取测试代币( 需要使用科学上网 )

---

## 如何获取`X-API-KEY`

* [ ] 首先打开[OKLink](https://www.oklink.com/)官网；
* [ ] 然后打开F12调试工具；
* [ ] 随意找到一个地址，点开它的交易列表；
* [ ] 然后在F12调试工具中打开网络选项，找到类似于以下信息的网络活动：
* [ ] conditionoffset=0&limit=20&address=0x*********************&nonzeroValue=false&t=1724280611286
* [ ] 打开后找到标头数据；
* [ ] 向下翻找到请求标头中的x-apikey，后面跟着的就是x-apikey。
* [ ] 附几个x-apikey，应该可以直接用：
* [ ] LWIzMWUtNDU0Ny05Mjk5LWI2ZDA3Yjc2MzFhYmEyYzkwM2NjfDI4MzcwODY2MDAwMjYzMzQ=
* [ ] LWIzMWUtNDU0Ny05Mjk5LWI2ZDA3Yjc2MzFhYmEyYzkwM2NjfDI4MzcwODY2NjM2ODgxMTk=
* [ ] LWIzMWUtNDU0Ny05Mjk5LWI2ZDA3Yjc2MzFhYmEyYzkwM2NjfDI4MzcwODY3MDYyMzkwNDk=

---

## 注意事项

* [ ] 本项目仅可用于当地法律允许的活动范围，不得使用本项目用于任何违法犯罪的活动！！！
* [ ] 不建议导入自己的公链账户，虽然私钥存储已加密，但还是可能会发生泄漏。
* [ ] 消息为明文发送，请勿发送重要或隐私数据否则可能会被监听（会尝试更新加密功能）；
* [ ] 本项目还处于开发早期,可能存在漏洞，可以反馈到Issues。
* [ ] 本项目使用Java17进行开发，请使用大于或等于Java17的版本运行。
* [ ] 欢迎大家对本项目提出建议，发到issue或在[BiliBili](https://space.bilibili.com/472452907)中私信我，看到就会回复。



---
