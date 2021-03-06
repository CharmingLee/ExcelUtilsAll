package com.charminglee911.excelutils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Author CharmingLee
 * Date 2017/4/1
 * Description 将Excel表中的内容生成对应的对象，第0行必须是对象的属性名，如果第0行和对象属性名不一致，可进行相关的关系映射
 *
 */
public class ExcelUtile {
    /**
     * Excel表中属性集合
     */
    private static List<String> fieldList = new ArrayList<>();
    /**
     * Excel表中属性和对象的关系映射
     */
    private static Map<String, String> fieldMapped = new  HashMap<>();

    private static final String LANG_STRING = "java.lang.String";
    private static final String LANG_INTEGER = "java.lang.Integer";
    private static final String LANG_DOUBLE = "java.lang.Double";
    private static final String LANG_SHORT = "java.lang.Short";
    private static final String LANG_LONG = "java.lang.Long";
    private static final String LANG_FLOAT = "java.lang.Float";
    private static final String LANG_BOOLEAN = "java.lang.Boolean";
    private static final String Excel2003 = "xls";
    private static final String Excel2007 = "xlsx";


    /**
     * 2007以下版本生成的xls格式的excle
     * @param xlsxIS    文件流
     * @param classe    要解析成的对象
     * @param <T>       泛型
     * @return          对象集合
     */
    @Deprecated
    public static<T> List<T> xlsToObj(InputStream xlsxIS, Class<T> classe) {
        return xlsToObj(xlsxIS, classe, null);
    }

    /**
     * 2007以上版本生成的xls格式的excle
     * @param xlsxIS    文件流
     * @param classe    要解析成的对象
     * @param <T>       泛型
     * @return          对象集合
     */
    @Deprecated
    public static<T> List<T> xlsxToObj(InputStream xlsxIS, Class<T> classe) {
        return xlsxToObj(xlsxIS, classe, null);
    }

    /**
     * 2007以下版本生成的xls格式的excle
     * @param xlsxIS    文件流
     * @param classe    要解析成的对象
     * @param mapped    对象属性和excle表中的字段的映射关系key为Excel表中的字段，value为classe中对应的属性名称
     * @param <T>       泛型
     * @return          对象集合
     */
    @Deprecated
    public static<T> List<T> xlsToObj(InputStream xlsxIS, Class<T> classe, Map<String, String> mapped) {
        List<T> list = null;
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(xlsxIS);
            list = excelToObj(workbook, classe, mapped);
        } catch (Exception e) {
            e.printStackTrace();

            throw new BusinessRuntimeException("Excel文件读取失败");
        }

        return list;
    }

    /**
     * 2007以上版本生成的xlsx格式的excle
     * @param xlsxIS    文件流
     * @param classe    要解析成的对象
     * @param mapped    对象属性和excle表中的字段的映射关系key为Excel表，value为classe中对应的属性名称
     * @param <T>       泛型
     * @return          对象集合
     */
    @Deprecated
    public static<T> List<T> xlsxToObj(InputStream xlsxIS, Class<T> classe, Map<String, String> mapped) {
        List<T> list = null;
        try {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(xlsxIS);
            list = excelToObj(xssfWorkbook, classe, mapped);
        } catch (Exception e) {
            e.printStackTrace();

            throw new BusinessRuntimeException("Excel文件读取失败");
        }

        return list;
    }

    /**
     * 从Excel文件中读取对象,根据file的文件扩展名识别xls、xlsx格式
     * @param file   Excel文件
     * @param classe 转换的目标对象类
     * @param <T>    泛型
     * @return       对象集合
     */
    public static<T> List<T> excelFileToObjects(File file, Class<T> classe){
        return excelFileToObjects(file, classe, null);
    }

    /**
     * 从Excel文件中读取对象,根据file的文件扩展名识别xls、xlsx格式
     * @param file   Excel文件
     * @param classe 转换的目标对象类
     * @param mapped 字段关系映射
     * @param <T>    泛型
     * @return       对象集合
     */
    public static<T> List<T> excelFileToObjects(File file, Class<T> classe, Map<String, String> mapped){
        List<T> list;
        try {
            FileInputStream fis = new FileInputStream(file);
            String fileName = file.getName();
            int index = fileName.lastIndexOf(".");
            String suffix = fileName.substring(index + 1);

            if (Excel2003.equals(suffix)){
                list = excelToObj(new HSSFWorkbook(fis), classe, mapped);
            } else if (Excel2007.equals(suffix)){
                list = excelToObj(new XSSFWorkbook(fis), classe, mapped);
            } else {
                throw new BusinessRuntimeException("文件格式不兼容");
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();

            throw new BusinessRuntimeException("对象生成失败");
        } catch (IOException e) {
            e.printStackTrace();

            throw new BusinessRuntimeException("文件操作异常");
        }

        return list;
    }

    /**
     * 创建Excel文本,根据file的文件扩展名识别xls、xlsx格式
     *
     * @param file 待创建的文件对象
     * @param list 创建的对象
     * @param <T>  泛型
     */
    public static<T> void objectsToExcelFile(File file, List<T> list) {
        if (file == null){
            throw new BusinessRuntimeException("文件不能为空");
        }

        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        String suffix = fileName.substring(index + 1);

        try {
            Workbook workbook;
            if (Excel2003.equals(suffix)){
                workbook = createBook(new HSSFWorkbook(), list);
            } else if (Excel2007.equals(suffix)){
                workbook = createBook(new XSSFWorkbook(), list);
            } else {
                throw new BusinessRuntimeException("文件格式不兼容");
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();

            throw new BusinessRuntimeException("文件写入失败");
        }
    }

    /**
     * 创建Excel对象
     * @param list 创建的对象
     * @param <T>  泛型
     * @return     Excel对象
     */
    private static<T> Workbook createBook(Workbook workbook, List<T> list){
        if (list.isEmpty()){
            return workbook;
        }

        Class clazz = list.get(0).getClass();
        String sheetName = null;
        if (clazz.isAnnotationPresent(ExcelSheet.class)){
            ExcelSheet sheetType = (ExcelSheet) clazz.getAnnotationsByType(ExcelSheet.class)[0];
            sheetName = sheetType.name();
        }

        List<String> sheetFields = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(ExcelField.class)){
                ExcelField excelField = field.getAnnotationsByType(ExcelField.class)[0];
                sheetFields.add(excelField.name());
            }
        }


        Sheet sheet;
        if (sheetName != null){
            sheet = workbook.createSheet(sheetName);
        } else {
            sheet = workbook.createSheet();
        }

        Row row = sheet.createRow(0);
        for (int i = 0; i < sheetFields.size(); i++) {
            row.createCell(i).setCellValue(sheetFields.get(i));
        }

        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            Row newRow = sheet.createRow(i + 1);
            try {
                createCell(t, newRow);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return workbook;
    }

    /**
     * 构建Excel的内容
     * @param t      单个对象
     * @param newRow Excel的行
     * @param <T>    泛型
     * @throws IllegalAccessException
     */
    private static <T> void createCell(T t, Row newRow) throws IllegalAccessException {
        Field[] declaredFields = t.getClass().getDeclaredFields();

        for (int j = 0; j < declaredFields.length; j++) {
            Field field = declaredFields[j];
            String fieldType = field.getType().getName();
            field.setAccessible(true);
            switch (fieldType){
                case LANG_STRING:
                    newRow.createCell(j).setCellValue(field.get(t).toString());
                    break;
                case LANG_INTEGER:
                    newRow.createCell(j).setCellValue(field.getInt(t));
                    break;
                case LANG_DOUBLE:
                    newRow.createCell(j).setCellValue(field.getDouble(t));
                    break;
                case LANG_SHORT:
                    newRow.createCell(j).setCellValue(field.getShort(t));
                    break;
                case LANG_LONG:
                    newRow.createCell(j).setCellValue(field.getLong(t));
                    break;
                case LANG_FLOAT:
                    newRow.createCell(j).setCellValue(field.getFloat(t));
                    break;
                case LANG_BOOLEAN:
                    newRow.createCell(j).setCellValue(field.getBoolean(t));
                    break;
            }
        }
    }

    /**
     * 解析excle中的字段成对象
     * @param workbook  excle对象
     * @param classe    要解析成的对象
     * @param mapped    对象属性和excle表中的字段的映射关系key为Excel表中的字段，value为classe中对应的属性名称
     * @param <T>       泛型
     * @return          对象集合
     * @throws Exception
     */
    private static<T> List<T> excelToObj(Workbook workbook, Class<T> classe, Map<String, String> mapped) throws ReflectiveOperationException {
        //创建对象集合
        List<T> list = new ArrayList<>();

        String sheetName = null;

        ExcelSheet sheetAnnotation = classe.getAnnotation(ExcelSheet.class);
        if (sheetAnnotation != null){
            sheetName = sheetAnnotation.name();
        }

        //循环所有表格生成对象
        for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
            Sheet sheet = workbook.getSheetAt(numSheet);
            if (sheet == null)
                continue;

            if (sheetName != null && !"".equals(sheetName) && !sheetName.equals(sheet.getSheetName())){
                continue;
            }

            //生成Excel表中的属性字段和对象属性的映射关系
            createFieldMapped(sheet, mapped, classe);

            //生成对象，并读取Excel表中的字段给对象设置相应属性，并添加到list中
            createObjs(list, sheet, classe);
        }

        fieldList = new ArrayList<>();
        fieldMapped = new  HashMap<>();

        return list;
    }

    /**
     * 生成Excel表中的属性字段和对象属性的映射关系
     * @param list 待生成Excel的对象集合
     * @param sheet Excel表的sheet
     * @param classe
     * @param <T>
     * @throws Exception
     */
    private static<T> void createObjs(List<T> list, Sheet sheet, Class<T> classe) throws ReflectiveOperationException {

        //第0行默认为对象属性，从第1行读取字段作为对象的属性
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null)
                continue;

            //创建一个对象
            T obj = classe.newInstance();
            list.add(obj);
            for (int i = 0 ; i < fieldList.size(); i++){
                //获取该列属性在对象中对应的属性
                String key = fieldList.get(i);
                key = fieldMapped.get(key);

                //excle表中的行
                Cell cell = row.getCell(i);

                //设置对象属性值
                setObjField(obj, classe, key, cell);
            }

        }

    }

    /**
     * 生成对象，并读取Excel表中的字段给对象设置相应属性，并添加到list中
     * @param sheet
     * @param mapped
     * @param classe
     */
    private static<T> void createFieldMapped(Sheet sheet, Map<String, String> mapped, Class<T> classe){
        //拿到第0行，每列默认为对象属性名
        Row fieldsRow = sheet.getRow(sheet.getFirstRowNum());
        if (fieldsRow == null){
            return;
        }

        //判断是否存在映射关系
        boolean isMapping = (mapped != null && !mapped.isEmpty());
        //判断是否存在注解映射
        boolean isAnnotation = isAnnotation(classe);

        for (short fieldIndex = fieldsRow.getFirstCellNum();
             fieldIndex < fieldsRow.getLastCellNum();
             fieldIndex++){

            Cell cell = fieldsRow.getCell(fieldIndex);
            String cellFiedl = getCellValue(cell);
            fieldList.add(cellFiedl);

            //处理对象属性和exle的映射
            if (isMapping){
                String value = mapped.get(cellFiedl);
                if (value != null && !"".equals(value)){
                    fieldMapped.put(cellFiedl, value);
                }else {
                    fieldMapped.put(cellFiedl, cellFiedl);
                }
            } else if (isAnnotation) {
                Field[] declaredFields = classe.getDeclaredFields();
                for (Field f : declaredFields) {
                    if (f.isAnnotationPresent(ExcelField.class)){
                        ExcelField annotation = f.getAnnotation(ExcelField.class);
                        fieldMapped.put(annotation.name(), f.getName());
                    }
                }
            } else { //没有映射关系，则默认使用表格中第0行作为对象的属性名
                fieldMapped.put(cellFiedl, cellFiedl);
            }

        }

    }

    /**
     * 判读是否注解映射
     * @param classe
     * @param <T>
     * @return
     */
    private static<T> boolean isAnnotation(Class<T> classe){
        boolean isTypeAnnotation = classe.isAnnotationPresent(ExcelSheet.class);
        if (isTypeAnnotation){
            return true;
        }

        Field[] declaredFields = classe.getDeclaredFields();
        for (Field f: declaredFields) {
            if (f.isAnnotationPresent(ExcelField.class)){
                return true;
            }
        }

        return false;
    }

    /**
     * 根据映射关系，给属性设置值
     * @param obj
     * @param classe
     * @param key
     * @param cell
     * @throws IllegalAccessException
     */
    private static void setObjField(Object obj, Class classe, String key, Cell cell) throws IllegalAccessException {
        //获取要设置的属性
        Field field = null;
        Field[] fields = classe.getDeclaredFields();
        for (Field f: fields) {
            if (f.getName().equals(key)){
                field = f;
                break;
            }
        }

        if (field == null)
            return;

        //判断field类型
        Object value = convertValue(field, cell);

        //设置属性
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 把cell的value转换成和对象一样的属性类型
     * @param field
     * @param cell
     * @return
     */
    private static Object convertValue(Field field, Cell cell){
        String type = field.getType().getName();
//        String cellValue = getCellValue(cell);

        if (LANG_STRING.equals(type)){
            return getCellValue(cell);
        }

        if ("int".equals(type) || LANG_INTEGER.equals(type)){
            Integer integer = 0;
            try {
                String cellValue = getCellValue(cell);
                double dValue = Double.valueOf(cellValue);
                if (dValue % 1 == 0)
                    integer = Integer.valueOf((int) dValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return integer;
        }

        if ("double".equals(type) || LANG_DOUBLE.equals(type)){
            Double aDouble = 0.0;
            try {
                aDouble = Double.valueOf(getCellValue(cell));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return aDouble;
        }

        if ("short".equals(type) || LANG_SHORT.equals(type)){
            Short value = 0;
            try {
                String cellValue = getCellValue(cell);
                double dValue = Double.valueOf(cellValue);
                if (dValue % 1 == 0)
                    value = Short.valueOf((short) dValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return value;
        }

        if ("long".equals(type) || LANG_LONG.equals(type)){
            Long value = 0L;
            try {
                String cellValue = getCellValue(cell);
                double dValue = Double.valueOf(cellValue);
                if (dValue % 1 == 0)
                    value = Long.valueOf((long) dValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return value;
        }

        if ("float".equals(type) || LANG_FLOAT.equals(type)){
            Float value = 0F;
            try {
                value = Float.valueOf(getCellValue(cell));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return value;
        }

        if ("boolean".equals(type) || LANG_BOOLEAN.equals(type)){
            Boolean value = false;
            try {
                value = Boolean.valueOf(getCellValue(cell));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return value;
        }

        if ("char".equals(type)){
            char value = 0;
            try {
                String sValue = String.valueOf(getCellValue(cell));
                if (sValue.length() > 0)
                    value = sValue.charAt(0);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return value;
        }

        return null;
    }

    /**
     * 从cell中获取Str值
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell){
        if (cell == null)
            return "";

        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
            return String.valueOf(cell.getBooleanCellValue());

        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
            return String.valueOf(cell.getNumericCellValue());

        if (cell.getCellType() == Cell.CELL_TYPE_STRING)
            return cell.getStringCellValue();

        return "";
    }

}

