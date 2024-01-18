package com.tiger;


import com.tiger.money.DbMan;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class MAIStockEOD {
    private static final String[] SECTORS = {"agro","consump","fincial","indus","propcon","resourc","service","tech"};
    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        for (String sector:SECTORS) {
            WebDriver driver = new FirefoxDriver();

            String url = "https://www.set.or.th/en/market/index/mai/" + sector;
            System.out.println("sector = "+sector+", url="+ url);
            driver.get(url);
            driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0,500)", "");

            /**
             * 1st table is factory tables
             * composed by 2 top tr tabletitle and Factory tr
             * others trs will be requrired for group-row-accordion-children-0-x
             */
            //List<WebElement> tables = driver.findElements(By.cssSelector("table"));

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            Connection con = null;
            try {
                try {
                    java.sql.Date date = new java.sql.Date(dateFormat.parse(args[0]).getTime());
                    DecimalFormat volFormat = new DecimalFormat("#,##0");
                    DecimalFormat valueFormat = new DecimalFormat("#,##0.00");
                    con = DbMan.getConnection();
                    PreparedStatement pstmt = con.prepareStatement(
                            "INSERT INTO settradedb.stockeod(symbol,openp,highp,lowp,closep,volume,value,date) VALUES(?,?,?,?,?,?,?,?);");
                    List<WebElement> elements = driver.findElements(By.cssSelector("table"));
                    elements = elements.subList(3,4);
                    WebElement stockTable = elements.get(0);

                    WebElement tbody = stockTable.findElements(By.xpath("./descendant::tbody")).get(0);
                    List<WebElement> trs = tbody.findElements(By.tagName("tr"));
                    for (WebElement tr: trs) {
                        List<WebElement> c = tr.findElements(By.xpath("./child::*"));
                        String symbol = c.get(0).findElement(By.cssSelector(".symbol")).getText();
                        String openStr = c.get(1).getText().trim();
                        String highStr = c.get(2).getText().trim();
                        String lowStr = c.get(3).getText().trim();
                        String closeStr = c.get(4).getText().trim();
                        String volumeStr = c.get(9).getText().trim();
                        String valueStr = c.get(10).getText().trim();

                        Float open = openStr.trim().equals("-") ? null : Float.parseFloat(openStr);
                        Float high = highStr.trim().equals("-") ? null : Float.parseFloat(highStr);
                        Float low = lowStr.trim().equals("-") ? null : Float.parseFloat(lowStr);
                        Float close = closeStr.trim().equals("-") ? null : Float.parseFloat(closeStr);
                        Long volume = volumeStr.trim().equals("-") ? null : (Long) volFormat.parse(volumeStr);
                        Double value = valueStr.trim().equals("-") ? null : valueFormat.parse(valueStr).doubleValue();

                        // setvalue to pstmt
                        pstmt.setString(1, symbol);
                        pstmt.setObject(2, open);
                        pstmt.setObject(3, high);
                        pstmt.setObject(4, low);
                        pstmt.setObject(5, close);
                        pstmt.setObject(6, volume);
                        pstmt.setObject(7, value);
                        pstmt.setObject(8, date);

                        pstmt.addBatch();
                        System.out.println("symbol=" + symbol + ", openStr=" + openStr);
                    }



                    int[] ints = pstmt.executeBatch();

                } finally {
                    if (con != null) {
                        con.close();
                    }
                }
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
            driver.close();
        }
        System.out.println("ok");

    }
}
