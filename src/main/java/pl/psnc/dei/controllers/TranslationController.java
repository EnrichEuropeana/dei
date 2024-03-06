package pl.psnc.dei.controllers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.dei.controllers.requests.TranslationRequest;
import pl.psnc.dei.service.translation.TranslationService;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {

	private final Logger logger = LoggerFactory.getLogger(TranslationController.class);

	private final TranslationService translationService;

	public TranslationController(TranslationService translationService) {
		this.translationService = translationService;
	}

	/**
	 * @return HTTP response code
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> applyTranslations(@RequestBody TranslationRequest translationRequest) {
		if (invalidRequest(translationRequest)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		try {
			translationService.applyTranslations(translationRequest.getXmlFolder(), translationRequest.getTranslationsFolder(), translationRequest.getFieldName());
		} catch (IOException | XPathExpressionException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private boolean invalidRequest(TranslationRequest translationRequest) {
		return translationRequest == null ||
				StringUtils.isBlank(translationRequest.getXmlFolder()) ||
				StringUtils.isBlank(translationRequest.getTranslationsFolder()) ||
				StringUtils.isBlank(translationRequest.getFieldName());
	}
}
