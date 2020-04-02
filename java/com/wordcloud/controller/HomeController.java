package com.wordcloud.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.snu.ids.ha.index.Keyword;
import org.snu.ids.ha.index.KeywordExtractor;
import org.snu.ids.ha.index.KeywordList;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController 
{
	private static boolean done = false;
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	private static Vector<String> lexicon = new Vector<String>();
	private static HashMap<String, String> mapper = new HashMap<String, String>();
	private static Vector<Integer> postingFile = new Vector<Integer>();
	private static Vector<Integer> termTable = new Vector<Integer>();
	
	private static KeywordExtractor ke = new KeywordExtractor();
	
	/**
	 * Simply selects the home view to render by returning its name.
	 * @throws Exception 
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) throws Exception 
	{
		logger.info("Welcome home! The client locale is {}.", locale);
		
		if (!done)
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\rhyme\\Desktop\\repo\\lexicon.txt"), "MS949"));
			String[] words = br.readLine().split(" ");
			for (int i=0; i< words.length; i++)
				lexicon.add(words[i]);
		
			br.close();
			br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\rhyme\\Desktop\\repo\\mapper.txt"), "UTF-8"));
			String[] map = br.readLine().split("%%");
			for (int i=0; i< map.length; i++)
			{
				String[] mapped = map[i].split(">>");
				mapper.put(mapped[1], mapped[0]);
			}
		
			br.close();
			br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\rhyme\\Desktop\\repo\\postingFile.dat")));
			String[] records = br.readLine().split(" ");
			for (String record : records)
				postingFile.add(Integer.parseInt(record));
		
			br.close();
			br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\rhyme\\Desktop\\repo\\termTable.txt"), "MS949"));
			String[] tableDat = br.readLine().split(" ");

			for (String loc : tableDat)
			{
				String[] termWithLoc = loc.split(":");
				termTable.add(Integer.parseInt(termWithLoc[1]));
			}
			done = true;
		}
			return "home";
	}

	@RequestMapping(value="/data/worddata.csv", method=RequestMethod.GET, produces="text/csv")
	public ResponseEntity csvOpen(HttpServletResponse response) throws Exception
	{
		try {
	        File file = new File("C:\\Users\\rhyme\\Desktop\\repo\\worddata.csv");
	        String reportName = "worddata";
	        return ResponseEntity.ok()
	                .header("Content-Disposition", "attachment; filename=" + reportName + ".csv")
	                .contentLength(file.length())
	                .contentType(MediaType.parseMediaType("text/csv"))
	                .body(new FileSystemResource(file));
					
	    } catch (Exception e) {
	    	e.printStackTrace();
	        throw new Exception();
	    } 
	}
	
	@RequestMapping(value="/query.do", method=RequestMethod.POST)
	public String webQuery(HttpServletRequest req, Model model) throws Exception
	{
		String query = req.getParameter("query");
		KeywordList kl = ke.extractKeyword(query, true);
		List<Integer> keywords = new ArrayList<Integer>();
		Vector<String> records = new Vector<String>();
		List<String> response = new ArrayList<String>();
		
		for (int i=0; i<kl.size(); i++)
		{
			String kwrd = kl.get(i).getString();
			int idx = lexicon.indexOf(kwrd);
			if (idx < 0) continue;
			
			if (!keywords.contains(kwrd)) keywords.add(idx);
			int sidx = termTable.elementAt(idx);
			int fidx = termTable.elementAt(idx + 1);
			for (int j= sidx; j< fidx; j++)
			{
				// Query with N-Keywords, conduct AND operation. ('OR, NOT' not materialized.)
				if (!records.contains("vector" + postingFile.elementAt(j) + ".txt"))
					records.add("vector"+  postingFile.elementAt(j) + ".txt");
			}
		}
		
		// using Bubble Sort(Time Complexity=> θ(n^2))
		Vector<Integer> rank = new Vector<Integer>();
		for (int i=0; i< records.size(); i++)
		{
			rank.add(0);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("C:\\Users\\rhyme\\Desktop\\repo\\docVector\\" + records.get(i)), "UTF-8"));
			String[] documentVector = br.readLine().split(" ");
			for (int j=0; j < documentVector.length; j++)
			{
				String[] vec = documentVector[j].split(":");
				if (keywords.contains(vec[0])) rank.set(i, rank.get(i) +1);
			}
		}
		
		int tmp;
		String stmp;
		for (int i=0; i< rank.size(); i++)
		{
			for (int j=i; j< rank.size(); j++)
			{
				if (rank.get(i) < rank.get(j))
				{
					tmp = rank.get(i);
					rank.set(i, rank.get(j));
					rank.set(j, tmp);
					
					stmp = records.get(i);
					records.set(i, records.get(j));
					records.set(j, stmp);
				}
			}
		}
		for (int i=0; i< rank.size(); i++)
			response.add(mapper.get(records.get(i)));
		
		model.addAttribute("result", response.toString());
		
		return "searchResult";
	}
	
	@RequestMapping(value="/category/{category}", method=RequestMethod.POST)
	public String categoryPolitics(@PathVariable Integer category, Model model) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\rhyme\\Desktop\\repo\\classification.txt"), "MS949"));
		String[] classification = br.readLine().split(" ");
		List<String> docInCategory = new ArrayList<String>();
		
		for (int i=0; i< classification.length; i++)
		{
			String[] docWithCategory = classification[i].split(":");
			if (Integer.parseInt(docWithCategory[1]) == category)
				docInCategory.add(mapper.get(docWithCategory[0]));
		}
		
		model.addAttribute("result", docInCategory.toString());
		return "searchResult";
	}
}