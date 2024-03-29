package pl.psnc.dei.util;

import lombok.experimental.UtilityClass;
import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.model.Aggregator;

import java.util.*;

@UtilityClass
public class IiifValidator {

	private static final List<String> ALLOWED_TYPES = new ArrayList<>();

	static {
		ALLOWED_TYPES.add("image/jp2");
		ALLOWED_TYPES.add("image/jpeg");
		ALLOWED_TYPES.add("image/tiff");
		ALLOWED_TYPES.add("image/png");
		ALLOWED_TYPES.add("image/bmp");
		ALLOWED_TYPES.add("application/pdf");
	}

	private static final Map<String, String> TYPES_SHORT_TO_FULL = new HashMap<>();

	static {
		TYPES_SHORT_TO_FULL.put("323","text/h323");
		TYPES_SHORT_TO_FULL.put("aaf","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("aca","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("accdb","application/msaccess");
		TYPES_SHORT_TO_FULL.put("accde","application/msaccess");
		TYPES_SHORT_TO_FULL.put("accdt","application/msaccess");
		TYPES_SHORT_TO_FULL.put("acx","application/internet-property-stream");
		TYPES_SHORT_TO_FULL.put("afm","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("ai","application/postscript");
		TYPES_SHORT_TO_FULL.put("aif","audio/x-aiff");
		TYPES_SHORT_TO_FULL.put("aifc","audio/aiff");
		TYPES_SHORT_TO_FULL.put("aiff","audio/aiff");
		TYPES_SHORT_TO_FULL.put("application","application/x-ms-application");
		TYPES_SHORT_TO_FULL.put("art","image/x-jg");
		TYPES_SHORT_TO_FULL.put("asd","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("asf","video/x-ms-asf");
		TYPES_SHORT_TO_FULL.put("asi","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("asm","text/plain");
		TYPES_SHORT_TO_FULL.put("asr","video/x-ms-asf");
		TYPES_SHORT_TO_FULL.put("asx","video/x-ms-asf");
		TYPES_SHORT_TO_FULL.put("atom","application/atom+xml");
		TYPES_SHORT_TO_FULL.put("au","audio/basic");
		TYPES_SHORT_TO_FULL.put("avi","video/x-msvideo");
		TYPES_SHORT_TO_FULL.put("axs","application/olescript");
		TYPES_SHORT_TO_FULL.put("bas","text/plain");
		TYPES_SHORT_TO_FULL.put("bcpio","application/x-bcpio");
		TYPES_SHORT_TO_FULL.put("bin","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("bmp","image/bmp");
		TYPES_SHORT_TO_FULL.put("c","text/plain");
		TYPES_SHORT_TO_FULL.put("cab","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("calx","application/vnd.ms-office.calx");
		TYPES_SHORT_TO_FULL.put("cat","application/vnd.ms-pki.seccat");
		TYPES_SHORT_TO_FULL.put("cdf","application/x-cdf");
		TYPES_SHORT_TO_FULL.put("chm","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("class","application/x-java-applet");
		TYPES_SHORT_TO_FULL.put("clp","application/x-msclip");
		TYPES_SHORT_TO_FULL.put("cmx","image/x-cmx");
		TYPES_SHORT_TO_FULL.put("cnf","text/plain");
		TYPES_SHORT_TO_FULL.put("cod","image/cis-cod");
		TYPES_SHORT_TO_FULL.put("cpio","application/x-cpio");
		TYPES_SHORT_TO_FULL.put("cpp","text/plain");
		TYPES_SHORT_TO_FULL.put("crd","application/x-mscardfile");
		TYPES_SHORT_TO_FULL.put("crl","application/pkix-crl");
		TYPES_SHORT_TO_FULL.put("crt","application/x-x509-ca-cert");
		TYPES_SHORT_TO_FULL.put("csh","application/x-csh");
		TYPES_SHORT_TO_FULL.put("css","text/css");
		TYPES_SHORT_TO_FULL.put("csv","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("cur","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("dcr","application/x-director");
		TYPES_SHORT_TO_FULL.put("deploy","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("der","application/x-x509-ca-cert");
		TYPES_SHORT_TO_FULL.put("dib","image/bmp");
		TYPES_SHORT_TO_FULL.put("dir","application/x-director");
		TYPES_SHORT_TO_FULL.put("disco","text/xml");
		TYPES_SHORT_TO_FULL.put("dll","application/x-msdownload");
		TYPES_SHORT_TO_FULL.put("dll.config","text/xml");
		TYPES_SHORT_TO_FULL.put("dlm","text/dlm");
		TYPES_SHORT_TO_FULL.put("doc","application/msword");
		TYPES_SHORT_TO_FULL.put("docm","application/vnd.ms-word.document.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		TYPES_SHORT_TO_FULL.put("dot","application/msword");
		TYPES_SHORT_TO_FULL.put("dotm","application/vnd.ms-word.template.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("dotx","application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		TYPES_SHORT_TO_FULL.put("dsp","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("dtd","text/xml");
		TYPES_SHORT_TO_FULL.put("dvi","application/x-dvi");
		TYPES_SHORT_TO_FULL.put("dwf","drawing/x-dwf");
		TYPES_SHORT_TO_FULL.put("dwp","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("dxr","application/x-director");
		TYPES_SHORT_TO_FULL.put("eml","message/rfc822");
		TYPES_SHORT_TO_FULL.put("emz","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("eot","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("eps","application/postscript");
		TYPES_SHORT_TO_FULL.put("etx","text/x-setext");
		TYPES_SHORT_TO_FULL.put("evy","application/envoy");
		TYPES_SHORT_TO_FULL.put("exe","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("exe.config","text/xml");
		TYPES_SHORT_TO_FULL.put("fdf","application/vnd.fdf");
		TYPES_SHORT_TO_FULL.put("fif","application/fractals");
		TYPES_SHORT_TO_FULL.put("fla","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("flr","x-world/x-vrml");
		TYPES_SHORT_TO_FULL.put("flv","video/x-flv");
		TYPES_SHORT_TO_FULL.put("gif","image/gif");
		TYPES_SHORT_TO_FULL.put("gtar","application/x-gtar");
		TYPES_SHORT_TO_FULL.put("gz","application/x-gzip");
		TYPES_SHORT_TO_FULL.put("h","text/plain");
		TYPES_SHORT_TO_FULL.put("hdf","application/x-hdf");
		TYPES_SHORT_TO_FULL.put("hdml","text/x-hdml");
		TYPES_SHORT_TO_FULL.put("hhc","application/x-oleobject");
		TYPES_SHORT_TO_FULL.put("hhk","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("hhp","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("hlp","application/winhlp");
		TYPES_SHORT_TO_FULL.put("hqx","application/mac-binhex40");
		TYPES_SHORT_TO_FULL.put("hta","application/hta");
		TYPES_SHORT_TO_FULL.put("htc","text/x-component");
		TYPES_SHORT_TO_FULL.put("htm","text/html");
		TYPES_SHORT_TO_FULL.put("html","text/html");
		TYPES_SHORT_TO_FULL.put("htt","text/webviewhtml");
		TYPES_SHORT_TO_FULL.put("hxt","text/html");
		TYPES_SHORT_TO_FULL.put("ico","image/x-icon");
		TYPES_SHORT_TO_FULL.put("ics","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("ief","image/ief");
		TYPES_SHORT_TO_FULL.put("iii","application/x-iphone");
		TYPES_SHORT_TO_FULL.put("inf","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("ins","application/x-internet-signup");
		TYPES_SHORT_TO_FULL.put("isp","application/x-internet-signup");
		TYPES_SHORT_TO_FULL.put("IVF","video/x-ivf");
		TYPES_SHORT_TO_FULL.put("jar","application/java-archive");
		TYPES_SHORT_TO_FULL.put("java","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("jck","application/liquidmotion");
		TYPES_SHORT_TO_FULL.put("jcz","application/liquidmotion");
		TYPES_SHORT_TO_FULL.put("jfif","image/pjpeg");
		TYPES_SHORT_TO_FULL.put("jp2", "image/jp2");
		TYPES_SHORT_TO_FULL.put("jpb","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("jpe","image/jpeg");
		TYPES_SHORT_TO_FULL.put("jpeg","image/jpeg");
		TYPES_SHORT_TO_FULL.put("jpg","image/jpeg");
		TYPES_SHORT_TO_FULL.put("js","application/x-javascript");
		TYPES_SHORT_TO_FULL.put("jsx","text/jscript");
		TYPES_SHORT_TO_FULL.put("latex","application/x-latex");
		TYPES_SHORT_TO_FULL.put("lit","application/x-ms-reader");
		TYPES_SHORT_TO_FULL.put("lpk","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("lsf","video/x-la-asf");
		TYPES_SHORT_TO_FULL.put("lsx","video/x-la-asf");
		TYPES_SHORT_TO_FULL.put("lzh","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("m13","application/x-msmediaview");
		TYPES_SHORT_TO_FULL.put("m14","application/x-msmediaview");
		TYPES_SHORT_TO_FULL.put("m1v","video/mpeg");
		TYPES_SHORT_TO_FULL.put("m3u","audio/x-mpegurl");
		TYPES_SHORT_TO_FULL.put("man","application/x-troff-man");
		TYPES_SHORT_TO_FULL.put("manifest","application/x-ms-manifest");
		TYPES_SHORT_TO_FULL.put("map","text/plain");
		TYPES_SHORT_TO_FULL.put("mdb","application/x-msaccess");
		TYPES_SHORT_TO_FULL.put("mdp","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("me","application/x-troff-me");
		TYPES_SHORT_TO_FULL.put("mht","message/rfc822");
		TYPES_SHORT_TO_FULL.put("mhtml","message/rfc822");
		TYPES_SHORT_TO_FULL.put("mid","audio/mid");
		TYPES_SHORT_TO_FULL.put("midi","audio/mid");
		TYPES_SHORT_TO_FULL.put("mix","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("mmf","application/x-smaf");
		TYPES_SHORT_TO_FULL.put("mno","text/xml");
		TYPES_SHORT_TO_FULL.put("mny","application/x-msmoney");
		TYPES_SHORT_TO_FULL.put("mov","video/quicktime");
		TYPES_SHORT_TO_FULL.put("movie","video/x-sgi-movie");
		TYPES_SHORT_TO_FULL.put("mp2","video/mpeg");
		TYPES_SHORT_TO_FULL.put("mp3","audio/mpeg");
		TYPES_SHORT_TO_FULL.put("mpa","video/mpeg");
		TYPES_SHORT_TO_FULL.put("mpe","video/mpeg");
		TYPES_SHORT_TO_FULL.put("mpeg","video/mpeg");
		TYPES_SHORT_TO_FULL.put("mpg","video/mpeg");
		TYPES_SHORT_TO_FULL.put("mpp","application/vnd.ms-project");
		TYPES_SHORT_TO_FULL.put("mpv2","video/mpeg");
		TYPES_SHORT_TO_FULL.put("ms","application/x-troff-ms");
		TYPES_SHORT_TO_FULL.put("msi","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("mso","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("mvb","application/x-msmediaview");
		TYPES_SHORT_TO_FULL.put("mvc","application/x-miva-compiled");
		TYPES_SHORT_TO_FULL.put("nc","application/x-netcdf");
		TYPES_SHORT_TO_FULL.put("nsc","video/x-ms-asf");
		TYPES_SHORT_TO_FULL.put("nws","message/rfc822");
		TYPES_SHORT_TO_FULL.put("ocx","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("oda","application/oda");
		TYPES_SHORT_TO_FULL.put("odc","text/x-ms-odc");
		TYPES_SHORT_TO_FULL.put("ods","application/oleobject");
		TYPES_SHORT_TO_FULL.put("one","application/onenote");
		TYPES_SHORT_TO_FULL.put("onea","application/onenote");
		TYPES_SHORT_TO_FULL.put("onetoc","application/onenote");
		TYPES_SHORT_TO_FULL.put("onetoc2","application/onenote");
		TYPES_SHORT_TO_FULL.put("onetmp","application/onenote");
		TYPES_SHORT_TO_FULL.put("onepkg","application/onenote");
		TYPES_SHORT_TO_FULL.put("osdx","application/opensearchdescription+xml");
		TYPES_SHORT_TO_FULL.put("p10","application/pkcs10");
		TYPES_SHORT_TO_FULL.put("p12","application/x-pkcs12");
		TYPES_SHORT_TO_FULL.put("p7b","application/x-pkcs7-certificates");
		TYPES_SHORT_TO_FULL.put("p7c","application/pkcs7-mime");
		TYPES_SHORT_TO_FULL.put("p7m","application/pkcs7-mime");
		TYPES_SHORT_TO_FULL.put("p7r","application/x-pkcs7-certreqresp");
		TYPES_SHORT_TO_FULL.put("p7s","application/pkcs7-signature");
		TYPES_SHORT_TO_FULL.put("pbm","image/x-portable-bitmap");
		TYPES_SHORT_TO_FULL.put("pcx","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("pcz","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("pdf","application/pdf");
		TYPES_SHORT_TO_FULL.put("pfb","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("pfm","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("pfx","application/x-pkcs12");
		TYPES_SHORT_TO_FULL.put("pgm","image/x-portable-graymap");
		TYPES_SHORT_TO_FULL.put("pko","application/vnd.ms-pki.pko");
		TYPES_SHORT_TO_FULL.put("pma","application/x-perfmon");
		TYPES_SHORT_TO_FULL.put("pmc","application/x-perfmon");
		TYPES_SHORT_TO_FULL.put("pml","application/x-perfmon");
		TYPES_SHORT_TO_FULL.put("pmr","application/x-perfmon");
		TYPES_SHORT_TO_FULL.put("pmw","application/x-perfmon");
		TYPES_SHORT_TO_FULL.put("png","image/png");
		TYPES_SHORT_TO_FULL.put("pnm","image/x-portable-anymap");
		TYPES_SHORT_TO_FULL.put("pnz","image/png");
		TYPES_SHORT_TO_FULL.put("pot","application/vnd.ms-powerpoint");
		TYPES_SHORT_TO_FULL.put("potm","application/vnd.ms-powerpoint.template.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("potx","application/vnd.openxmlformats-officedocument.presentationml.template");
		TYPES_SHORT_TO_FULL.put("ppam","application/vnd.ms-powerpoint.addin.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("ppm","image/x-portable-pixmap");
		TYPES_SHORT_TO_FULL.put("pps","application/vnd.ms-powerpoint");
		TYPES_SHORT_TO_FULL.put("ppsm","application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("ppsx","application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		TYPES_SHORT_TO_FULL.put("ppt","application/vnd.ms-powerpoint");
		TYPES_SHORT_TO_FULL.put("pptm","application/vnd.ms-powerpoint.presentation.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation");
		TYPES_SHORT_TO_FULL.put("prf","application/pics-rules");
		TYPES_SHORT_TO_FULL.put("prm","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("prx","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("ps","application/postscript");
		TYPES_SHORT_TO_FULL.put("psd","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("psm","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("psp","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("pub","application/x-mspublisher");
		TYPES_SHORT_TO_FULL.put("qt","video/quicktime");
		TYPES_SHORT_TO_FULL.put("qtl","application/x-quicktimeplayer");
		TYPES_SHORT_TO_FULL.put("qxd","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("ra","audio/x-pn-realaudio");
		TYPES_SHORT_TO_FULL.put("ram","audio/x-pn-realaudio");
		TYPES_SHORT_TO_FULL.put("rar","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("ras","image/x-cmu-raster");
		TYPES_SHORT_TO_FULL.put("rf","image/vnd.rn-realflash");
		TYPES_SHORT_TO_FULL.put("rgb","image/x-rgb");
		TYPES_SHORT_TO_FULL.put("rm","application/vnd.rn-realmedia");
		TYPES_SHORT_TO_FULL.put("rmi","audio/mid");
		TYPES_SHORT_TO_FULL.put("roff","application/x-troff");
		TYPES_SHORT_TO_FULL.put("rpm","audio/x-pn-realaudio-plugin");
		TYPES_SHORT_TO_FULL.put("rtf","application/rtf");
		TYPES_SHORT_TO_FULL.put("rtx","text/richtext");
		TYPES_SHORT_TO_FULL.put("scd","application/x-msschedule");
		TYPES_SHORT_TO_FULL.put("sct","text/scriptlet");
		TYPES_SHORT_TO_FULL.put("sea","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("setpay","application/set-payment-initiation");
		TYPES_SHORT_TO_FULL.put("setreg","application/set-registration-initiation");
		TYPES_SHORT_TO_FULL.put("sgml","text/sgml");
		TYPES_SHORT_TO_FULL.put("sh","application/x-sh");
		TYPES_SHORT_TO_FULL.put("shar","application/x-shar");
		TYPES_SHORT_TO_FULL.put("sit","application/x-stuffit");
		TYPES_SHORT_TO_FULL.put("sldm","application/vnd.ms-powerpoint.slide.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("sldx","application/vnd.openxmlformats-officedocument.presentationml.slide");
		TYPES_SHORT_TO_FULL.put("smd","audio/x-smd");
		TYPES_SHORT_TO_FULL.put("smi","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("smx","audio/x-smd");
		TYPES_SHORT_TO_FULL.put("smz","audio/x-smd");
		TYPES_SHORT_TO_FULL.put("snd","audio/basic");
		TYPES_SHORT_TO_FULL.put("snp","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("spc","application/x-pkcs7-certificates");
		TYPES_SHORT_TO_FULL.put("spl","application/futuresplash");
		TYPES_SHORT_TO_FULL.put("src","application/x-wais-source");
		TYPES_SHORT_TO_FULL.put("ssm","application/streamingmedia");
		TYPES_SHORT_TO_FULL.put("sst","application/vnd.ms-pki.certstore");
		TYPES_SHORT_TO_FULL.put("stl","application/vnd.ms-pki.stl");
		TYPES_SHORT_TO_FULL.put("sv4cpio","application/x-sv4cpio");
		TYPES_SHORT_TO_FULL.put("sv4crc","application/x-sv4crc");
		TYPES_SHORT_TO_FULL.put("svg","image/svg+xml");
		TYPES_SHORT_TO_FULL.put("swf","application/x-shockwave-flash");
		TYPES_SHORT_TO_FULL.put("t","application/x-troff");
		TYPES_SHORT_TO_FULL.put("tar","application/x-tar");
		TYPES_SHORT_TO_FULL.put("tcl","application/x-tcl");
		TYPES_SHORT_TO_FULL.put("tex","application/x-tex");
		TYPES_SHORT_TO_FULL.put("texi","application/x-texinfo");
		TYPES_SHORT_TO_FULL.put("texinfo","application/x-texinfo");
		TYPES_SHORT_TO_FULL.put("tgz","application/x-compressed");
		TYPES_SHORT_TO_FULL.put("thmx","application/vnd.ms-officetheme");
		TYPES_SHORT_TO_FULL.put("thn","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("tif","image/tiff");
		TYPES_SHORT_TO_FULL.put("tiff","image/tiff");
		TYPES_SHORT_TO_FULL.put("toc","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("tr","application/x-troff");
		TYPES_SHORT_TO_FULL.put("trm","application/x-msterminal");
		TYPES_SHORT_TO_FULL.put("tsv","text/tab-separated-values");
		TYPES_SHORT_TO_FULL.put("ttf","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("txt","text/plain");
		TYPES_SHORT_TO_FULL.put("u32","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("uls","text/iuls");
		TYPES_SHORT_TO_FULL.put("ustar","application/x-ustar");
		TYPES_SHORT_TO_FULL.put("vbs","text/vbscript");
		TYPES_SHORT_TO_FULL.put("vcf","text/x-vcard");
		TYPES_SHORT_TO_FULL.put("vcs","text/plain");
		TYPES_SHORT_TO_FULL.put("vdx","application/vnd.ms-visio.viewer");
		TYPES_SHORT_TO_FULL.put("vml","text/xml");
		TYPES_SHORT_TO_FULL.put("vsd","application/vnd.visio");
		TYPES_SHORT_TO_FULL.put("vss","application/vnd.visio");
		TYPES_SHORT_TO_FULL.put("vst","application/vnd.visio");
		TYPES_SHORT_TO_FULL.put("vsto","application/x-ms-vsto");
		TYPES_SHORT_TO_FULL.put("vsw","application/vnd.visio");
		TYPES_SHORT_TO_FULL.put("vsx","application/vnd.visio");
		TYPES_SHORT_TO_FULL.put("vtx","application/vnd.visio");
		TYPES_SHORT_TO_FULL.put("wav","audio/wav");
		TYPES_SHORT_TO_FULL.put("wax","audio/x-ms-wax");
		TYPES_SHORT_TO_FULL.put("wbmp","image/vnd.wap.wbmp");
		TYPES_SHORT_TO_FULL.put("wcm","application/vnd.ms-works");
		TYPES_SHORT_TO_FULL.put("wdb","application/vnd.ms-works");
		TYPES_SHORT_TO_FULL.put("wks","application/vnd.ms-works");
		TYPES_SHORT_TO_FULL.put("wm","video/x-ms-wm");
		TYPES_SHORT_TO_FULL.put("wma","audio/x-ms-wma");
		TYPES_SHORT_TO_FULL.put("wmd","application/x-ms-wmd");
		TYPES_SHORT_TO_FULL.put("wmf","application/x-msmetafile");
		TYPES_SHORT_TO_FULL.put("wml","text/vnd.wap.wml");
		TYPES_SHORT_TO_FULL.put("wmlc","application/vnd.wap.wmlc");
		TYPES_SHORT_TO_FULL.put("wmls","text/vnd.wap.wmlscript");
		TYPES_SHORT_TO_FULL.put("wmlsc","application/vnd.wap.wmlscriptc");
		TYPES_SHORT_TO_FULL.put("wmp","video/x-ms-wmp");
		TYPES_SHORT_TO_FULL.put("wmv","video/x-ms-wmv");
		TYPES_SHORT_TO_FULL.put("wmx","video/x-ms-wmx");
		TYPES_SHORT_TO_FULL.put("wmz","application/x-ms-wmz");
		TYPES_SHORT_TO_FULL.put("wps","application/vnd.ms-works");
		TYPES_SHORT_TO_FULL.put("wri","application/x-mswrite");
		TYPES_SHORT_TO_FULL.put("wrl","x-world/x-vrml");
		TYPES_SHORT_TO_FULL.put("wrz","x-world/x-vrml");
		TYPES_SHORT_TO_FULL.put("wsdl","text/xml");
		TYPES_SHORT_TO_FULL.put("wvx","video/x-ms-wvx");
		TYPES_SHORT_TO_FULL.put("x","application/directx");
		TYPES_SHORT_TO_FULL.put("xaf","x-world/x-vrml");
		TYPES_SHORT_TO_FULL.put("xaml","application/xaml+xml");
		TYPES_SHORT_TO_FULL.put("xap","application/x-silverlight-app");
		TYPES_SHORT_TO_FULL.put("xbap","application/x-ms-xbap");
		TYPES_SHORT_TO_FULL.put("xbm","image/x-xbitmap");
		TYPES_SHORT_TO_FULL.put("xdr","text/plain");
		TYPES_SHORT_TO_FULL.put("xht","application/xhtml+xml");
		TYPES_SHORT_TO_FULL.put("xhtml","application/xhtml+xml");
		TYPES_SHORT_TO_FULL.put("xla","application/vnd.ms-excel");
		TYPES_SHORT_TO_FULL.put("xlam","application/vnd.ms-excel.addin.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("xlc","application/vnd.ms-excel");
		TYPES_SHORT_TO_FULL.put("xlm","application/vnd.ms-excel");
		TYPES_SHORT_TO_FULL.put("xls","application/vnd.ms-excel");
		TYPES_SHORT_TO_FULL.put("xlsb","application/vnd.ms-excel.sheet.binary.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("xlsm","application/vnd.ms-excel.sheet.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		TYPES_SHORT_TO_FULL.put("xlt","application/vnd.ms-excel");
		TYPES_SHORT_TO_FULL.put("xltm","application/vnd.ms-excel.template.macroEnabled.12");
		TYPES_SHORT_TO_FULL.put("xltx","application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		TYPES_SHORT_TO_FULL.put("xlw","application/vnd.ms-excel");
		TYPES_SHORT_TO_FULL.put("xml","text/xml");
		TYPES_SHORT_TO_FULL.put("xof","x-world/x-vrml");
		TYPES_SHORT_TO_FULL.put("xpm","image/x-xpixmap");
		TYPES_SHORT_TO_FULL.put("xps","application/vnd.ms-xpsdocument");
		TYPES_SHORT_TO_FULL.put("xsd","text/xml");
		TYPES_SHORT_TO_FULL.put("xsf","text/xml");
		TYPES_SHORT_TO_FULL.put("xsl","text/xml");
		TYPES_SHORT_TO_FULL.put("xslt","text/xml");
		TYPES_SHORT_TO_FULL.put("xsn","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("xtp","application/octet-stream");
		TYPES_SHORT_TO_FULL.put("xwd","image/x-xwindowdump");
		TYPES_SHORT_TO_FULL.put("z","application/x-compress");
		TYPES_SHORT_TO_FULL.put("zip","application/x-zip-compressed");
	}

	/**
	 * Checks if IIF is available or conversion is possible for given record.
	 *
	 * @param aggregator aggregator object
	 * @param record   record json-ld object
	 * @param mimeType record's mimeType
	 * @return {@link IiifAvailability}
	 */
	public static IiifAvailability checkIfIiifAvailable(Aggregator aggregator, JsonObject record, String mimeType) {
		if (record == null) {
			return IiifAvailability.DATA_UNAVAILABLE;
		}
		if (IiifChecker.checkIfIiif(record, aggregator)) {
			return IiifAvailability.AVAILABLE;
		}
		if (ALLOWED_TYPES.contains(mimeType)) {
			//todo remove below if statement when ddb binaries endpoint become available
			if (aggregator == Aggregator.DDB) {
				return IiifAvailability.CONVERSION_IMPOSSIBLE;
			}
			return IiifAvailability.CONVERSION_POSSIBLE;
		}
		return IiifAvailability.CONVERSION_IMPOSSIBLE;
	}

	public static boolean isMimeTypeAllowed(String mimeType) {
		return ALLOWED_TYPES.contains(mimeType);
	}

	public static String getMimeTypeFromShort(String shortMimeType) {
		if (Objects.isNull(shortMimeType)) {
			return null;
		}
		return TYPES_SHORT_TO_FULL.get(shortMimeType.toLowerCase());
	}
}
