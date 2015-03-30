
/*
 * �������(����)�κ��� ���� ������ �о�� �Ľ��ϴ� Ŭ����
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ssg.chat.SubjectInfo;

public class ExcelHandler {
	
	/*
	 * ���� ������ �Ľ��Ͽ� ����Ʈ�� �����Ѵ�.
	 */
	public static ArrayList<SubjectInfo> ExcelParser() {
		File file = new File("./data/201302.xlsx");
		XSSFWorkbook wb = null;

		try {
			wb = new XSSFWorkbook(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ���� ������ ������� ����Ʈ
		ArrayList<SubjectInfo> subjInfoList = new ArrayList<SubjectInfo>();
		SubjectInfo si = null;

		for (Row row : wb.getSheetAt(0)) {
			si = new SubjectInfo();
			String[] temp = new String[32];
			int count = 0;

			for (Cell cell : row) {
				temp[count] = cell.getRichStringCellValue().getString();
				count++;
			}
			si.setSubjectName(temp[4]);		// �����
			si.setSubjectNo(temp[2]);		// �м���ȣ
			si.setClassNo(temp[3]);			// �й�
			
			subjInfoList.add(si);
		}
		
		return subjInfoList;
	}
}
