# PiCommander
使用Android App控制與監控Raspberry Pi連接的硬體設備，例如LED與蜂鳴器。連接繼電器模組，也可以控制家用電器。

## 歷程

* 2015/03/18。開放原始程式碼，請參考[這裡](https://github.com/macdidi5/PiCommander/blob/master/ProjectNotes.md)的說明。
* 2015/03/18。支援GPIO擴充晶片：MCP23008與MCP23017，請參考[這裡](https://github.com/macdidi5/PiCommander/blob/master/SupportGPIOExpander.md)。
* 2015/03/16。覺得少了什麼？有錯誤？在[這裡](https://github.com/macdidi5/PiCommander/issues)留下你的想法！
* 2015/03/16。開放PiCommander v0.1應用程式。[InstallAndPlay.md](https://github.com/macdidi5/PiCommander/blob/master/InstallAndPlay.md)

## 示範

實際操作影片：

https://youtu.be/Reqdk4hRZKo

Raspberry Pi連接的零件，有三個LED、蜂鳴器，還有透過繼電器模組控制桌燈：

![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander010.jpg)
![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander011.jpg)

## 需求
硬體：

* [Raspberry Pi Model B+](http://www.raspberrypi.org/products/model-b-plus/)或[Raspberry Pi 2 Model B](http://www.raspberrypi.org/products/raspberry-pi-2-model-b/)。
* 8 GB MicroSD 記憶卡。
* 5V 2A 電源供應器。
* 無線USB網路卡。[Edimax EW-7811Un](http://www.edimax.com/tw/produce_detail.php?pd_id=301&pl1_id=24&pl2_id=116)。
* Android行動電話，Android 4.0.3或更新的版本。
* 使用Raspberry Pi控制設備需要的零件，例如麵包板、連接線、LED、蜂鳴器、繼電器模組與家用電器。

軟體：

* [Java SE 8 for ARM](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-arm-downloads-2187472.html)。
* [Mosquitto](http://mosquitto.org/)，An Open Source MQTT v3.1/v3.1.1 Broker。
* [Paho](https://eclipse.org/paho/)，MQTT v3 Java用戶端類別庫。

開發環境：

* Java SE 8
* NetBeans
* Android Studio

## 架構

MQTT（Message Queuing Telemetry Transport）是由IBM與Eurotech共同研發的通訊協定。在2014年11月，MQTT v3.1.1已經成為[OASIS標準](https://www.oasis-open.org/news/announcements/mqtt-version-3-1-1-becomes-an-oasis-standard)。MQTT是應用在Machine to Machine（M2M）與Internet of Things（IoT）的通訊協定，透過發佈（publish）與訂閱（subscribe）的方式來傳遞訊息，特色是需要很低的硬體資源與網路頻寬。

PiCommander使用MQTT在不同類型的裝置傳送與接收訊息：

![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander001.png)

PiCommander包含下列三個部份：

1. 安裝在Raspberry Pi的MQTT Broker：
	* MQTT Broker有很多選擇，這裡採用[Mosquitto](http://mosquitto.org/)。
	* 讓用戶端發佈與訂閱訊息。
2. 使用Java技術開發，採用[Paho](https://eclipse.org/paho/)類別庫，在Raspberry Pi運作的PiCommanderService應用程式。
	* 訂閱並處理Android發佈的訊息。
	* 控制連接在GPIO的零件與設備。
	* 發佈GPIO狀態變化的訊息。
3. 使用Android技術開發，採用[Paho](https://eclipse.org/paho/)類別庫，在Android裝置運作的PiCommander App。
	* 提供使用者設定與操作的畫面。
	* 發佈控制GPIO狀態的訊息。
	* 訂閱狀態變化的訊息，在畫面顯示目前最新的GPIO狀態。

## 功能

啟動在Raspberry Pi的PiCommanderService以後，就可以在Android裝置執行下列的操作：

1. 啟動PiCommand App，選擇連線到MQTT Broker：

	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander002.png)

2. 輸入MQTT Broker（Raspberry PI）的IP位址後選擇連線：

	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander003.png)

3. 連線到MQTT Broker以後，選擇新增控制項：

	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander004.png)

4. 選擇已經連接好設備與零件的GPIO針腳編號（[Pi4J](http://pi4j.com/pins/model-2b-rev1.html)），輸入設備的說明後選擇新增：

	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander005.png)

5. 新增後的控制項可以控制指定的GPIO針腳，切換為高電壓或低電壓：

	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander006.png)	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander007.png)

6. 在Raspberry Pi連接更多設備與零件以後，使用同樣的方式新增與設定控制項：

	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander008.png)

7. 不需要的控制項可以在畫面長按後選擇刪除：

	![](https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander009.png)	

## 試試看

安裝執行環境與功能測試請參考[InstallAndPlay.md](https://github.com/macdidi5/PiCommander/blob/master/InstallAndPlay.md)。

## 下一階段

公開原始程式碼與相關實作的說明。



