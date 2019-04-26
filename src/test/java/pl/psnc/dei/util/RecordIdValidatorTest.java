package pl.psnc.dei.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class RecordIdValidatorTest {

	@Test
	public void shouldValidateRecordId() {
		String testRecordId1 = "/12345/12345_my_record";
		String testRecordId2 = "/12345678/12345";
		String testRecordId3 = "/12345/_my_record_";
		String testRecordId4 = "/123/record";
		String testRecordId5 = "/123/1234_żółć.żółw";
		String testRecordId6 = "/123/1234_Jean-Marie_Le'Blanc";
		String testRecordId7 = "/123/1234_Ὀδυσσεύς";

		Assert.assertTrue(RecordIdValidator.validate(testRecordId1));
		Assert.assertTrue(RecordIdValidator.validate(testRecordId2));
		Assert.assertTrue(RecordIdValidator.validate(testRecordId3));
		Assert.assertTrue(RecordIdValidator.validate(testRecordId4));
		Assert.assertTrue(RecordIdValidator.validate(testRecordId5));
		Assert.assertTrue(RecordIdValidator.validate(testRecordId6));
		Assert.assertTrue(RecordIdValidator.validate(testRecordId7));
	}

	@Test
	public void shouldNotValidateRecordId() {
		String testRecordId1 = "/12345_12345_my_record";
		String testRecordId2 = "/12345/my record";
		String testRecordId3 = "/abc/12345_my_record";
		String testRecordId4 = "/12345_123/12345_my_record";
		String testRecordId5 = "12345/12345_my_record";

		Assert.assertFalse(RecordIdValidator.validate(testRecordId1));
		Assert.assertFalse(RecordIdValidator.validate(testRecordId2));
		Assert.assertFalse(RecordIdValidator.validate(testRecordId3));
		Assert.assertFalse(RecordIdValidator.validate(testRecordId4));
		Assert.assertFalse(RecordIdValidator.validate(testRecordId5));
	}
}
