package org.example;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.example.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*  В проекте ословные технологии это Selenium - для получения html кода страницы
    jsoup - для анализа html кода страницы
    apache.poi - для работы с .xls файлом
*/

public class App {

    static String chromeDriver = "webdriver.chrome.driver"; // Хром драйвер
    static String wayToChromeDriver = "/Users/ivanantonovskiy/gameWord/untitled5/selenium/chromedriver";  // путь к папке в которой лежит драйвер (на новой системе надо изменить )
    static String forGetItemByAttributeMenuItem = "menu-item";  // Атрибут по которому я получаю категории товаров
    static String classForSearchHtmlElements = "class";
    static String hrefItem = "href";  // атрибут для получения ссылки на товар если добавить urlLoadingPageMetro
    static String altItem = "alt"; // атрибут для получения имя товара
    static String urlLoadingPageMetro = "https://online.metro-cc.ru";  // ссылка на магазин
    static String roleForSearchHtmlElements = "role"; // атрибут для получения остатка товара
    static String forGetItemByAttributeNavigation = "navigation"; // атрибут для получения количества страниц в категории товара
    static String dataQaForSearchHtmlElements = "data-qa"; // ссылка для получения ссылки на товар
    static String pageReadingMode = "?order=price_asc&page="; // указывает режим чтения страницы ( здесь к примеру указан режим который моказывает товары от меньшего к большему)
    static String firstCategoryOfGoods = "Алкоголь"; // категория с которой нужно парсить
    static String lastCategoryOfGoods = "Замороженные готовые блюда";  // категория до которой нужно парсить
    static String fileType = ".xls";  // тип файла в который парсится (в этом приложение парсинг производится только в ексель файл)
    static String pathToTheFolder = "/Users/ivanantonovskiy/lists/";  // путь к папке в которой должны лежать файлы с товаром
    static WebDriver webDriverConfig() {
        System.setProperty(chromeDriver, wayToChromeDriver);
        WebDriver webDriver = new ChromeDriver();
        return webDriver;
    }
    public static Elements catalogAllProductUrl(String urlProductList, String firstWordSubString, String secondWordSubString) {
        WebDriver webDriver = webDriverConfig();
        webDriver.get(urlProductList);
        String pageSource = webDriver.getPageSource();
        pageSource = pageSource.substring(pageSource.indexOf(firstWordSubString), pageSource.indexOf(secondWordSubString) + 1);
        Document temp = Jsoup.parse(pageSource);
        Elements category = temp.getElementsByAttributeValue(classForSearchHtmlElements, forGetItemByAttributeMenuItem);
        webDriver.quit();
        return category;
    }
    /*
    функция catalogAllProductUrl - получает список сылок на все категории товаров в магазине и обрезает его с classForSearchHtmlElements
    по secondWordSubString ,для того чтобы получить только нужные категории товаров
    */
    static boolean isTheLinkFinal(String href , String hrefTwo) {
        return hrefTwo.contains(href);
    }
    /*
    функция isTheLinkFinal - определяет , конечная ли эта ссылка (конечная ссылка - эта ссвлка которая сразу нас перебрасывает
    на страницу с одним видом товара)
    */
    static List<String> listUrlWithProductCategories(String urlProductList, String firstWordSubString, String secondWordSubString) {
        List<String> category = new ArrayList<>();
        Elements elements = catalogAllProductUrl(urlProductList, firstWordSubString, secondWordSubString);
        for (int i = 0 ; i < elements.size();i++) {
            if(i+1 != elements.size()) {
                if (!isTheLinkFinal(elements.get(i).attr(hrefItem), elements.get(i + 1).attr(hrefItem))) {
                    String url = urlLoadingPageMetro + elements.get(i).attr(hrefItem);
                    category.add(url);
                }
            }
        }
        return category;
    }
    /*
    функция listUrlWithProductCategories - возвращает лист стрингов(конечные ссылки на нужные виды товров
    */
    static int getProductPagesByLink(String url) {
        WebDriver webDriver = webDriverConfig();
        webDriver.get(url);
        String pageSource = webDriver.getPageSource();
        Document doc = Jsoup.parse(pageSource);
        Elements navigator = doc.getElementsByAttributeValue(roleForSearchHtmlElements, forGetItemByAttributeNavigation);
        if(navigator.size() == 0){
            return 0;
        } else {
            String countPage = navigator.text();
            int lastPage = 1;
            if (countPage.contains("... ")) {
                countPage = countPage.replace("... ", "");
                String[] cPage = countPage.split(" ");
                lastPage = Integer.parseInt(cPage[cPage.length - 1]);
            } else if (!countPage.contains("... ") && countPage.length() > 3) {
                String[] cPage = countPage.split(" ");
                lastPage = Integer.parseInt(cPage[cPage.length - 1]);
            }
            webDriver.quit();
            return lastPage;
        }
    }
    /*
    функция getProductPagesByLink - возвращает количество страниц товаров ктегории по ссылке
     */
    static void addProductsByLinkToTheProductList(String url, List<Product> products) {
        int pages = getProductPagesByLink(url);
        if(pages > 0) {
            for (int j = 1; j <= pages; j++) {
                WebDriver webDriver = webDriverConfig();
                webDriver.get(url + pageReadingMode + j);
                String pageSource = webDriver.getPageSource();
                webDriver.quit();
                Document doc = Jsoup.parse(pageSource);
                Elements po = doc.getElementsByAttributeValue(dataQaForSearchHtmlElements, "product-card-name");
                Elements cost = doc.getElementsByAttributeValue(classForSearchHtmlElements, "base-product-item__content-details");
                Elements remainder = doc.getElementsByAttributeValue(dataQaForSearchHtmlElements, "product-card-dropdown-button-stocks");
                for (int i = 0; i < po.size(); i++) {
                    String costs = cost.get(i).text().replace(" ", "").toLowerCase();
                    String cos = cost.get(i).text().substring(0, cost.get(i).text().indexOf(" "));

                    if (!cos.equals("Раскупили")) {
                        costs = costs.substring(0, costs.indexOf("д"));
                        Product product = new Product(getAProductCategory(url)
                                , po.get(i).attr(altItem)
                                , Double.parseDouble(costs)
                                , remainder.get(i).text(), urlLoadingPageMetro + po.get(i).attr(hrefItem));
                        products.add(product);

                    }

                }

            }

        }
    }
    /*
    функция addProductsByLinkToTheProductList - заполняеет продукт лист всеми товарами  по ссылке на выбранную категорию
     */
    static String getAProductCategory(String category) {
        String temp = category;
        return temp.substring(category.lastIndexOf("/" ) + 1);
    }
    /*
    функция getAProductCategory - возвращает категорию товара , которую достает из ссылки
     */
    static void creatingAnExelFileForTheProductCategory (String pathToTheFolder , String fileType, List<Product> products, int category_i) throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(products.get(0).getFirstCategory());
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 17000);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 7000);
        sheet.setColumnWidth(5, 30000);
        FileOutputStream fos = new FileOutputStream(pathToTheFolder + products.get(0).getFirstCategory() + fileType);
        for (int i = 0; i < products.size() + 1; i++) {
            Row row = sheet.createRow(i);
            if (i == 0) {
                Cell head0 = row.createCell(0);
                head0.setCellValue("ID");
                Cell head1 = row.createCell(1);
                head1.setCellValue("Категория");
                Cell head4 = row.createCell(2);
                head4.setCellValue("Название");
                Cell head5 = row.createCell(3);
                head5.setCellValue("стоимость");
                Cell head6 = row.createCell(4);
                head6.setCellValue("наличие");
                Cell head7 = row.createCell(5);
                head7.setCellValue("ссылка");
            } else {
                for (int j = 0; j < 6; j++) {
                    Cell cell = row.createCell(j);
                    switch (j) {
                        case 0:
                            int id = (category_i + 1) * 2000 + i;
                            cell.setCellValue("№" + id);
                            break;
                        case 1:
                            cell.setCellValue(products.get(i - 1).getFirstCategory());
                            break;
                        case 2:
                            cell.setCellValue(products.get(i - 1).getProductName());
                            break;
                        case 3:
                            cell.setCellValue(products.get(i - 1).getCost());
                            break;
                        case 4:
                            cell.setCellValue(products.get(i - 1).getRemainder());
                            break;
                        case 5:
                            cell.setCellValue(products.get(i - 1).getUrl());
                            break;
                    }
                }
            }
        }
        wb.write(fos);
        fos.close();
        wb.close();
    }
    /*
    функция creatingAnExelFileForTheProductCategory - создает ексель лист используя продукт лист и category_i(любое число - нужно для id товара(в моем случае))
     */
    public static void main(String[] args) throws IOException {

        List<String> urlCategory = listUrlWithProductCategories(urlLoadingPageMetro, firstCategoryOfGoods, lastCategoryOfGoods);
        for (int category_i = 120  ; category_i < urlCategory.size();category_i++) {
            List<Product> products = new ArrayList<>();
            try {
                addProductsByLinkToTheProductList(urlCategory.get(category_i), products);
            } catch (WebDriverException e){
                addProductsByLinkToTheProductList(urlCategory.get(category_i), products);
            }
            if (products.size() > 0) {
                creatingAnExelFileForTheProductCategory(pathToTheFolder,fileType,products,category_i);
            }
        }
    }


}



