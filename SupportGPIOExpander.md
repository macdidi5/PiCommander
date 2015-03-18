# 支援GPIO擴充功能

支援GPIO擴充晶片，在Raspberry Pi連接MCP23008或MCP23017以後，讓Android App可以控制更多的GPIO。

示範影片：

https://youtu.be/AQggjulIgzo

示範圖片：

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander020.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander020.png" width="480"/></a>

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander021.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander021.png"/></a><a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander022.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander022.png"/></a>

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander023.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander023.png"/></a><a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander024.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander024.png"/></a>

## 支援的GPIO擴充晶片

* MCP23008，擴充8個GPIO

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander014.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander014.png" width="480"/></a>

* MCP23017，擴充16個GPIO

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander015.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander015.png" width="480"/></a>

IIC通訊協定，可以串接，位址可以設定為0x20到0x27。如果串接MCP23017八個，可以擴充128個GPIO。

## MCP23008線路圖

採用預設的IIC位址0x20：

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander016.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander016.png" width="600"/></a>

## MCP23017線路圖

設定IIC的位址為0x21：

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander017.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander017.png" width="600"/></a>

## IIC位址設定

MCP23008與MCP23017用來設定位址的腳位：

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander018.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander018.png" width="600"/></a>

下面的表格表示0x20到0x27的I2C設備位址與它們的對應設定。以0x21的I2C設備位址來說，A2與A1針腳為「0」，表示它們要連接到接地。A0針腳為「1」表示它要連接到3.3V：

<a href="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander019.png"><img src="https://github.com/macdidi5/PiCommander/blob/master/images/PiCommander019.png"/></a>

===============
線路圖與說明來自[http://www.kingsinfo.com.tw/module.php?i=item&tid=8213](http://www.kingsinfo.com.tw/module.php?i=item&tid=8213)
