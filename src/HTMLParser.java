import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParser
{
    List<String> words;
    Pattern htmlPattern = Pattern.compile("<html>.*</html>", Pattern.DOTALL);

    HTMLParser(List<String> words)
    {
        this.words = words;
    }

    public String parseDangerousWords(String html)
    {
        Matcher htmlMatcher = htmlPattern.matcher(html);
        String result = html;

        if(htmlMatcher.find())
        {
            for (String word : words)
            {
                String wordRegex = "[\\d\\W]" + word + "[\\d\\W]";
                Pattern wordPattern = Pattern.compile(wordRegex);
                Matcher wordMatcher = wordPattern.matcher(html);

                result = wordMatcher.replaceAll("<span style=\"color: red\">" + "$0" + "</span>");
            }
        }
        return result;
    }
}
