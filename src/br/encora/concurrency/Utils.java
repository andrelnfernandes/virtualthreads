package br.encora.concurrency;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

public class Utils {
	/**
	 * This method create a file and returns in a string with: . The start time .
	 * The end time . The execution time of the method . The name of the file . The
	 * size of the file
	 * 
	 * @param i Is a sequential number only to create a unique name to file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	// private static final int ONE_KBYTE = 1024;
	private static final int ONE_MEGABYTE = 1024 * 1024;
	private static final String FOLDER_FILE = "C:\\TestFiles\\raw\\";
	private static final Charset CHARSET_ASCII = Charset.forName("ASCII");
	private static final SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss.SSS");
	public static final SimpleDateFormat sdfTimeExec = new SimpleDateFormat("mm:ss.SSS");
	

	/**
	 * Generate file. Used only for load testing in the threads demo project
	 * 
	 * @param i
	 * @param folder
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String generateFile(int i, String folder, boolean callUrlService, 
			int stepCallUrl, String typeExecutor)
			throws FileNotFoundException, IOException {
		Instant startTime = Instant.now();
		String beginDate = sdfDate.format(Date.from(startTime));
		
		//long startTime = System.currentTimeMillis();		
		//String beginDate = sdfDate.format(new Date(startTime));

		String filename = newFileName(i, folder);
		FileOutputStream fos = new FileOutputStream(filename);
		String text = "File Name: " + filename + "\n" + "Begin Execution: " + beginDate + "\n";

		byte[] byteArrray = text.getBytes(CHARSET_ASCII);
		fos.write(byteArrray);
		fos.close();
		fos = null;

		// the file is closed and reopened just to waste a little more time since we are
		// testing threads running concurrently
		fos = new FileOutputStream(filename);
		String endDate = sdfDate.format(new Date());
		

		// these String reassignments are proposital and done to generate
		// more garbage collection and memory occupation
		// In production, we must use a StringBuilder or StringBufer
		if (callUrlService && isMultiple(i, stepCallUrl)) {
			text = text + "World time: " + getWorldTimeFromAleatoryWorldTimezone() + "\n\n";
		}

		// Only to create a huge file
		String toRepeat = "xpto.";
		text = text + toRepeat.repeat(ONE_MEGABYTE / toRepeat.length()) + "\n\n";
		
		text = text + "End Execution: " + endDate + "\n" + "Execution time: "
				+ (Duration.between(startTime, Instant.now()).toMillis()) + "\n\n";		

		byteArrray = text.getBytes(CHARSET_ASCII);
		fos.write(byteArrray); // rewrite from begining
		fos.close();
		fos = null;
		
		Instant endTime = Instant.now();
		endDate = sdfDate.format(Date.from(endTime));
		Duration executionTime = Duration.between(startTime, endTime);
		
		
		//System.out.println("executing thread number " + i + " Begin Execution: " + beginDate + " End execution:" + endDate +   " - Execution Time:" + sdfTimeExec.format(executionTime));
		return typeExecutor + "executing thread number " + i + " Begin Execution: " + beginDate + " End execution:" + endDate +   " - Execution Time:" + sdfTimeExec.format(executionTime.toMillis()) +"\n";
	}

	public static String deleteFile(int i, String filename) {		
		File file = new File(filename); 
		String message = null;
		if(file.delete())
			message = "deleted the file " + filename +"\n";
		else {
			message = "FAILED to delete " + filename +"\n";
		}
		return message; 		
	}
	
	public static String deleteFile(int i) {		
		String filename = newFileName(i, null);		
		return deleteFile(i, filename);
	}	
	
	private static String newFileName(int i, String folder) {
		if (folder == null) {
			folder = FOLDER_FILE;
		}
		String filename = folder + "A_" + String.format("%05d", i) + ".txt";
		return filename;
	}

	/**
	 * Get the time for an aleatory timezone from the webservice in the
	 * worldtimeapi.org site
	 * 
	 * @param city
	 * @return
	 */
	public static String getWorldTimeFromAleatoryWorldTimezone() {
		String timezone = getAleatoryTimezone();
		return getWorldTime(timezone);
	}

	/**
	 * Get the time from the webservice in the worldtimeapi.org site
	 * 
	 * @param city
	 * @return
	 */
	public static String getWorldTime(String timezone) {
		String returnStringTime = "";

		String apiUrl = "http://worldtimeapi.org/api/timezone/" + timezone;

		HttpClient client = HttpClient.newHttpClient();
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl)).build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			returnStringTime = response.body();

		} catch (URISyntaxException | IOException | InterruptedException e) {
			returnStringTime = "There was an error accessing the worldtimeapi API: " + e.getMessage();

		}

		return returnStringTime;
	}

	/**
	 * Only for use with the threads demo project
	 * 
	 * @return a aleatory timezone
	 */
	public static String getAleatoryTimezone() {
		long seed = Instant.now().getEpochSecond() * 1000000;
		int index = new Random(seed).nextInt(timezones.length);

		return timezones[index];
	}

	/**
	 * Method for test if a mod of division is 0
	 * 
	 * @param num
	 * @param divisor
	 * @return
	 */
	private static boolean isMultiple(int num, int divisor) {
		return num % divisor == 0;
	}

	private static final String[] timezones = { "Africa/Abidjan", "Africa/Algiers", "Africa/Bissau", "Africa/Cairo",
			"Africa/Casablanca", "Africa/Ceuta", "Africa/El_Aaiun", "Africa/Johannesburg", "Africa/Juba",
			"Africa/Khartoum", "Africa/Lagos", "Africa/Maputo", "Africa/Monrovia", "Africa/Nairobi", "Africa/Ndjamena",
			"Africa/Sao_Tome", "Africa/Tripoli", "Africa/Tunis", "Africa/Windhoek", "America/Adak", "America/Anchorage",
			"America/Araguaina", "America/Argentina/Buenos_Aires", "America/Argentina/Catamarca",
			"America/Argentina/Cordoba", "America/Argentina/Jujuy", "America/Argentina/La_Rioja",
			"America/Argentina/Mendoza", "America/Argentina/Rio_Gallegos", "America/Argentina/Salta",
			"America/Argentina/San_Juan", "America/Argentina/San_Luis", "America/Argentina/Tucuman",
			"America/Argentina/Ushuaia", "America/Asuncion", "America/Bahia", "America/Bahia_Banderas",
			"America/Barbados", "America/Belem", "America/Belize", "America/Boa_Vista", "America/Bogota",
			"America/Boise", "America/Cambridge_Bay", "America/Campo_Grande", "America/Cancun", "America/Caracas",
			"America/Cayenne", "America/Chicago", "America/Chihuahua", "America/Ciudad_Juarez", "America/Costa_Rica",
			"America/Cuiaba", "America/Danmarkshavn", "America/Dawson", "America/Dawson_Creek", "America/Denver",
			"America/Detroit", "America/Edmonton", "America/Eirunepe", "America/El_Salvador", "America/Fort_Nelson",
			"America/Fortaleza", "America/Glace_Bay", "America/Goose_Bay", "America/Grand_Turk", "America/Guatemala",
			"America/Guayaquil", "America/Guyana", "America/Halifax", "America/Havana", "America/Hermosillo",
			"America/Indiana/Indianapolis", "America/Indiana/Knox", "America/Indiana/Marengo",
			"America/Indiana/Petersburg", "America/Indiana/Tell_City", "America/Indiana/Vevay",
			"America/Indiana/Vincennes", "America/Indiana/Winamac", "America/Inuvik", "America/Iqaluit",
			"America/Jamaica", "America/Juneau", "America/Kentucky/Louisville", "America/Kentucky/Monticello",
			"America/La_Paz", "America/Lima", "America/Los_Angeles", "America/Maceio", "America/Managua",
			"America/Manaus", "America/Martinique", "America/Matamoros", "America/Mazatlan", "America/Menominee",
			"America/Merida", "America/Metlakatla", "America/Mexico_City", "America/Miquelon", "America/Moncton",
			"America/Monterrey", "America/Montevideo", "America/New_York", "America/Nome", "America/Noronha",
			"America/North_Dakota/Beulah", "America/North_Dakota/Center", "America/North_Dakota/New_Salem",
			"America/Nuuk", "America/Ojinaga", "America/Panama", "America/Paramaribo", "America/Phoenix",
			"America/Port-au-Prince", "America/Porto_Velho", "America/Puerto_Rico", "America/Punta_Arenas",
			"America/Rankin_Inlet", "America/Recife", "America/Regina", "America/Resolute", "America/Rio_Branco",
			"America/Santarem", "America/Santiago", "America/Santo_Domingo", "America/Sao_Paulo",
			"America/Scoresbysund", "America/Sitka", "America/St_Johns", "America/Swift_Current", "America/Tegucigalpa",
			"America/Thule", "America/Tijuana", "America/Toronto", "America/Vancouver", "America/Whitehorse",
			"America/Winnipeg", "America/Yakutat", "Antarctica/Casey", "Antarctica/Davis", "Antarctica/Macquarie",
			"Antarctica/Mawson", "Antarctica/Palmer", "Antarctica/Rothera", "Antarctica/Troll", "Asia/Almaty",
			"Asia/Amman", "Asia/Anadyr", "Asia/Aqtau", "Asia/Aqtobe", "Asia/Ashgabat", "Asia/Atyrau", "Asia/Baghdad",
			"Asia/Baku", "Asia/Bangkok", "Asia/Barnaul", "Asia/Beirut", "Asia/Bishkek", "Asia/Chita", "Asia/Choibalsan",
			"Asia/Colombo", "Asia/Damascus", "Asia/Dhaka", "Asia/Dili", "Asia/Dubai", "Asia/Dushanbe", "Asia/Famagusta",
			"Asia/Gaza", "Asia/Hebron", "Asia/Ho_Chi_Minh", "Asia/Hong_Kong", "Asia/Hovd", "Asia/Irkutsk",
			"Asia/Jakarta", "Asia/Jayapura", "Asia/Jerusalem", "Asia/Kabul", "Asia/Kamchatka", "Asia/Karachi",
			"Asia/Kathmandu", "Asia/Khandyga", "Asia/Kolkata", "Asia/Krasnoyarsk", "Asia/Kuching", "Asia/Macau",
			"Asia/Magadan", "Asia/Makassar", "Asia/Manila", "Asia/Nicosia", "Asia/Novokuznetsk", "Asia/Novosibirsk",
			"Asia/Omsk", "Asia/Oral", "Asia/Pontianak", "Asia/Pyongyang", "Asia/Qatar", "Asia/Qostanay",
			"Asia/Qyzylorda", "Asia/Riyadh", "Asia/Sakhalin", "Asia/Samarkand", "Asia/Seoul", "Asia/Shanghai",
			"Asia/Singapore", "Asia/Srednekolymsk", "Asia/Taipei", "Asia/Tashkent", "Asia/Tbilisi", "Asia/Tehran",
			"Asia/Thimphu", "Asia/Tokyo", "Asia/Tomsk", "Asia/Ulaanbaatar", "Asia/Urumqi", "Asia/Ust-Nera",
			"Asia/Vladivostok", "Asia/Yakutsk", "Asia/Yangon", "Asia/Yekaterinburg", "Asia/Yerevan", "Atlantic/Azores",
			"Atlantic/Bermuda", "Atlantic/Canary", "Atlantic/Cape_Verde", "Atlantic/Faroe", "Atlantic/Madeira",
			"Atlantic/South_Georgia", "Atlantic/Stanley", "Australia/Adelaide", "Australia/Brisbane",
			"Australia/Broken_Hill", "Australia/Darwin", "Australia/Eucla", "Australia/Hobart", "Australia/Lindeman",
			"Australia/Lord_Howe", "Australia/Melbourne", "Australia/Perth", "Australia/Sydney", "CET", "CST6CDT",
			"EET", "EST", "EST5EDT", "Etc/GMT", "Etc/GMT+1", "Etc/GMT+10", "Etc/GMT+11", "Etc/GMT+12", "Etc/GMT+2",
			"Etc/GMT+3", "Etc/GMT+4", "Etc/GMT+5", "Etc/GMT+6", "Etc/GMT+7", "Etc/GMT+8", "Etc/GMT+9", "Etc/GMT-1",
			"Etc/GMT-10", "Etc/GMT-11", "Etc/GMT-12", "Etc/GMT-13", "Etc/GMT-14", "Etc/GMT-2", "Etc/GMT-3", "Etc/GMT-4",
			"Etc/GMT-5", "Etc/GMT-6", "Etc/GMT-7", "Etc/GMT-8", "Etc/GMT-9", "Etc/UTC", "Europe/Andorra",
			"Europe/Astrakhan", "Europe/Athens", "Europe/Belgrade", "Europe/Berlin", "Europe/Brussels",
			"Europe/Bucharest", "Europe/Budapest", "Europe/Chisinau", "Europe/Dublin", "Europe/Gibraltar",
			"Europe/Helsinki", "Europe/Istanbul", "Europe/Kaliningrad", "Europe/Kirov", "Europe/Kyiv", "Europe/Lisbon",
			"Europe/London", "Europe/Madrid", "Europe/Malta", "Europe/Minsk", "Europe/Moscow", "Europe/Paris",
			"Europe/Prague", "Europe/Riga", "Europe/Rome", "Europe/Samara", "Europe/Saratov", "Europe/Simferopol",
			"Europe/Sofia", "Europe/Tallinn", "Europe/Tirane", "Europe/Ulyanovsk", "Europe/Vienna", "Europe/Vilnius",
			"Europe/Volgograd", "Europe/Warsaw", "Europe/Zurich", "HST", "Indian/Chagos", "Indian/Maldives",
			"Indian/Mauritius", "MET", "MST", "MST7MDT", "PST8PDT", "Pacific/Apia", "Pacific/Auckland",
			"Pacific/Bougainville", "Pacific/Chatham", "Pacific/Easter", "Pacific/Efate", "Pacific/Fakaofo",
			"Pacific/Fiji", "Pacific/Galapagos", "Pacific/Gambier", "Pacific/Guadalcanal", "Pacific/Guam",
			"Pacific/Honolulu", "Pacific/Kanton", "Pacific/Kiritimati", "Pacific/Kosrae", "Pacific/Kwajalein",
			"Pacific/Marquesas", "Pacific/Nauru", "Pacific/Niue", "Pacific/Norfolk", "Pacific/Noumea",
			"Pacific/Pago_Pago", "Pacific/Palau", "Pacific/Pitcairn", "Pacific/Port_Moresby", "Pacific/Rarotonga",
			"Pacific/Tahiti", "Pacific/Tarawa", "Pacific/Tongatapu", "WET" };

}
