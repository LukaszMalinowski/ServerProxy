import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParser
{
    //TODO popraw regegxy
    List<String> words;
    Pattern htmlPattern = Pattern.compile("<html.*>.*</html>", Pattern.DOTALL);
    Pattern bracketsPattern = Pattern.compile(">.*<", Pattern.DOTALL);

    HTMLParser(List<String> words)
    {
        this.words = words;
    }

    public String parseDangerousWords(String html)
    {
        Matcher htmlMatcher = htmlPattern.matcher(html);
        Matcher bracketsMatcher = bracketsPattern.matcher(html);

        String result = html;

        //sprawdzamy czy slowo znajduje się w ciele html'a
        if(htmlMatcher.find())
        {
            if(bracketsMatcher.find())
            {
                for (String word : words)
                {
                    //Sprawdzamy czy przed i za slowem nie znajduje sie znak mogacy byc skladowa innego slowa
                    String wordRegex = "[\\d\\W]" + word + "[\\d\\W]";
                    Pattern wordPattern = Pattern.compile(wordRegex);
                    Matcher wordMatcher = wordPattern.matcher(html);

                    result = wordMatcher.replaceAll("<span style=\"color: red\">" + "$0" + "</span>");
                }
            }
        }
        return result;
    }
}
