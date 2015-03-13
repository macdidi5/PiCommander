# PiCommander
使用Android App控制與監控Raspberry Pi連接的硬體設備，例如LED與蜂鳴器。連接繼電器模組，也可以控制家用電器。

## 需求
硬體設備：

* [Raspberry Pi Model B+](http://www.raspberrypi.org/products/model-b-plus/)或[Raspberry Pi 2 Model B](http://www.raspberrypi.org/products/raspberry-pi-2-model-b/)。
* 8 GB MicroSD 記憶卡。
* 5V 2A 電源供應器。
* 無線USB網路卡。[Edimax EW-7811Un](http://www.edimax.com/tw/produce_detail.php?pd_id=301&pl1_id=24&pl2_id=116)。
* 其它，例如麵包板、連接線、LED與蜂鳴器。
* Android行動電話。

軟體：

* [Java SE 8 for ARM](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-arm-downloads-2187472.html)。
* [Mosquitto](http://mosquitto.org/)，An Open Source MQTT v3.1/v3.1.1 Broker。

## 架構

## 安裝

### Raspberry Pi

1. 為Raspberry Pi安裝與設定好RASPBIAN作業系統。並確認下列項目：

	* 可以連線到網際網路。
	* 使用ifconfig指令查詢Raspberry Pi的IP位址。

2. 在工作電腦使用SSH連線到Raspberry Pi。
3. 執行下列的指令確認Raspberry Pi是否已經安裝Java SE 8：

		java -version
		
		java version "1.8.0"		Java(TM) SE Runtime Environment (build 1.8.0-b132)		Java HotSpot(TM) Client VM (build 25.0-b70, mixed mode)

4. 如果顯示下列的訊息，表示Raspberry Pi已經安裝Java SE 8：

		java version "1.8.0"		Java(TM) SE Runtime Environment (build 1.8.0-b132)		Java HotSpot(TM) Client VM (build 25.0-b70, mixed mode)

5. 執行下列的指令安裝Mosquitto（MQTT Broker Server）：

		apt-get install mosquitto

5. 執行下列的指令準備修改Raspberry Pi設定檔：

		sudo nano /etc/hosts

6. 參考下列的內容修改Raspberry Pi的IP位址：

		[Raspberry Pi的IP位址]		RaspberryPi

7. 依序按下「Ctrl+X」、「Enter」與「Y」，儲存檔案與結束nano。
8. 執行下列的指令重新啟動Raspberry Pi：

		sudo reboot

9. Raspberry Pi重新啟動以後，Mosquitto就會開始提供MQTT Broker服務。

### PiCommanderService for Raspberry Pi

1. 在工作電腦使用SSH連線到Raspberry Pi。
2. 執行下列的指令下載PiCommanderService：

	wget 

3. 執行下列的指令解壓縮下載的檔案：

		unzip PiCommanderService.zip

4. 執行下列的指令切換資料夾與啟動PiCommanderService：

		cd PiCommanderService
		sudo java -jar PiCommanderService.jar

5. 等候畫面出現下列的訊息，表示PiCommanderService已經提供服務：

		PiCommanderService ready...

### PiCommander for Android

1. x
2. x
3. x
4. x
5. x
6. x
7. x
8. x
9. x
10. x
11. x
12. x

## 運作
