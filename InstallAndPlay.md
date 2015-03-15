# 安裝與測試

## Raspberry Pi

1. 為Raspberry Pi安裝與設定好RASPBIAN作業系統。並確認下列項目：

	* 可以連線到網際網路。
	* 使用ifconfig指令查詢Raspberry Pi的IP位址。

2. 在工作電腦使用SSH連線到Raspberry Pi。
3. 執行下列的指令確認Raspberry Pi是否已經安裝Java SE 8：

		java -version
		
4. 如果顯示下列的訊息，表示Raspberry Pi已經安裝Java SE 8：

		java version "1.8.0"
		Java(TM) SE Runtime Environment (build 1.8.0-b132)
		Java HotSpot(TM) Client VM (build 25.0-b70, mixed mode)

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

## PiCommanderService for Raspberry Pi

1. 在工作電腦使用SSH連線到Raspberry Pi。
2. 執行下列的指令下載PiCommanderService：

		git clone https://github.com/macdidi5/PiCommander.git

3. 執行下列的指令切換資料夾與解壓縮下載的檔案：

		cd PiCommander/apps
		unzip PiCommanderService.zip

4. 執行下列的指令切換資料夾與啟動PiCommanderService：

		cd PiCommanderService
		sudo java -jar PiCommanderService.jar

5. 等候畫面出現下列訊息，表示PiCommanderService已經提供服務：

		PiCommanderService Ready...

6. 執行後續測試的時候，這個應用程式要保持在運作的壯態。按「Ctrl + X」可以結束PiCommanderService。

## 在Raspberry Pi連接測試用的零件

你可以參考下面的線路圖連接好測用的零件：

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander012.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander012.png" width="600"/></a>

連接繼電器模組以後也可以控制家用電器：

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander013.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander013.png" width="600"/></a>

## PiCommander for Android

1. 在工作電腦開啟瀏覽器，輸入下列的網址列下載檔案：

		https://github.com/macdidi5/PiCommander/archive/master.zip

2. 解壓縮下載的檔案，找到解壓縮目錄下的「apps/PiCommanderApp.zip」，解壓縮這個檔案以後，可以看到一個名稱為「mobile-debug.apk」的Android APK檔案。
3. 將你的Android行動電話連接到工作電腦，傳輸與安裝mobile-debug.apk。
4. 確認Raspberry Pi已經安裝並啟動PiCommanderService。
5. 確認行動電話連線到Raspberry Pi同一個區域網路。
6. 啟動安裝在行動電話的PiCommander，可以[參考這裡的說明](https://github.com/macdidi5/PiCommander#%E5%8A%9F%E8%83%BD)開始測試。
