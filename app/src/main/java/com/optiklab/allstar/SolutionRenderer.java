package com.optiklab.allstar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local, offline Markdown and TeX renderer. MathJax assets are bundled in the APK. */
public final class SolutionRenderer {
    private SolutionRenderer() {}

    public static String html(String markdown) {
        StringBuilder out = new StringBuilder(HEAD);
        if (markdown == null || markdown.trim().isEmpty()) {
            out.append("<div class='empty'>Fotoğrafı ekleyip çözüm istediğinde öğretmen anlatımı burada görünecek.</div>");
        } else {
            String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
            boolean inList = false;
            String listType = "";
            boolean inSection = false;
            boolean inMath = false;
            StringBuilder math = new StringBuilder();
            for (String raw : lines) {
                String line = raw.trim();
                if (inMath) {
                    if (line.endsWith("$$") || line.endsWith("\\]")) {
                        String tail = line.endsWith("$$") ? "$$" : "\\]";
                        math.append(line.substring(0, line.length() - tail.length()));
                        out.append("<div class='math-block'>\\[").append(esc(math.toString())).append("\\]</div>");
                        inMath = false;
                        math.setLength(0);
                    } else {
                        math.append(line).append("\n");
                    }
                    continue;
                }
                if (line.isEmpty()) {
                    if (inList) { out.append("</").append(listType).append(">"); inList = false; }
                    continue;
                }
                if (line.startsWith("$$") || line.startsWith("\\[")) {
                    String start = line.startsWith("$$") ? "$$" : "\\[";
                    String end = line.startsWith("$$") ? "$$" : "\\]";
                    int pos = line.indexOf(end, start.length());
                    if (pos >= 0) {
                        out.append("<div class='math-block'>\\[")
                            .append(esc(line.substring(start.length(), pos))).append("\\]</div>");
                    } else {
                        inMath = true;
                        math.append(line.substring(start.length())).append("\n");
                    }
                    continue;
                }
                if (line.matches("^[-_*]{3,}$")) {
                    if (inList) { out.append("</").append(listType).append(">"); inList = false; }
                    out.append("<hr/>"); continue;
                }
                boolean heading = line.startsWith("#") || line.matches("^\\*\\*[^*]+\\*\\*$");
                if (heading) {
                    if (inList) { out.append("</").append(listType).append(">"); inList = false; }
                    if (inSection) out.append("</section>");
                    String title = line.replaceFirst("^#{1,5}\\s*", "").replaceFirst("^\\*\\*", "").replaceFirst("\\*\\*$", "");
                    boolean step = title.toLowerCase(java.util.Locale.ROOT).contains("adım");
                    boolean tactic = title.toLowerCase(java.util.Locale.forLanguageTag("tr"))
                            .matches(".*(taktik|ipucu|strateji|hızlı yol|püf noktası).*");
                    out.append("<section class='").append(tactic ? "tactic-section" : step ? "step-section" : "section").append("'>");
                    out.append("<h2>").append(inline(title)).append("</h2>");
                    inSection = true;
                    continue;
                }
                boolean unordered = line.matches("^[-*•]\\s+.+");
                boolean ordered = line.matches("^\\d+[.)]\\s+.+");
                if (unordered || ordered) {
                    String next = ordered ? "ol" : "ul";
                    if (inList && !listType.equals(next)) { out.append("</").append(listType).append(">"); inList = false; }
                    if (!inList) { listType = next; out.append("<").append(listType).append(">"); inList = true; }
                    String item = line.replaceFirst(unordered ? "^[-*•]\\s+" : "^\\d+[.)]\\s+", "");
                    out.append("<li>").append(inline(item)).append("</li>");
                    continue;
                }
                if (inList) { out.append("</").append(listType).append(">"); inList = false; }
                String lower = line.toLowerCase(java.util.Locale.forLanguageTag("tr"));
                boolean clue = lower.startsWith("**neden") || lower.startsWith("**taktik")
                    || lower.startsWith("**önemli") || lower.startsWith("**dikkat")
                    || lower.startsWith("**nasıl fark") || lower.startsWith("**kontrol");
                out.append(clue ? "<p class='clue'>" : "<p>");
                out.append(inline(line)).append("</p>");
            }
            if (inMath) out.append("<p>").append(inline(math.toString())).append("</p>");
            if (inList) out.append("</").append(listType).append(">");
            if (inSection) out.append("</section>");
        }
        return out.append(FOOT).toString();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String inline(String s) {
        // Escape ALL user/model text before adding markup. No arbitrary model HTML executes.
        String escaped = esc(s);
        Matcher bold = Pattern.compile("\\*\\*(.+?)\\*\\*").matcher(escaped);
        StringBuffer b = new StringBuffer();
        while (bold.find()) bold.appendReplacement(b, "<strong>" + Matcher.quoteReplacement(bold.group(1)) + "</strong>");
        bold.appendTail(b);
        return b.toString().replaceAll("(?<!\\*)\\*([^*]+)\\*(?!\\*)", "<em>$1</em>");
    }

    private static final String HEAD = """
        <!DOCTYPE html><html lang="tr"><head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=4"/>
        <style>
        :root{color-scheme:dark;--fg:#edf2ff;--sub:#aeb9d0;--line:#303952;--accent:#91a6ff}
        html,body{margin:0;padding:0;background:#000;color:var(--fg);font-family:system-ui,-apple-system,'Roboto',sans-serif;font-size:16px;line-height:1.7;overflow-x:hidden}
        body{padding:4px 2px 22px;box-sizing:border-box}
        h2{font-size:18px;line-height:1.3;letter-spacing:-.2px;color:#fff;margin:3px 0 16px}
        p{font-size:15.5px;line-height:1.74;margin:11px 0;overflow-wrap:break-word}
        strong{color:#fff;font-weight:700}
        em{color:#c8d4ff}
        section{padding:18px 16px;margin:12px 0 16px;background:#111620;border:1px solid #2c3446;border-radius:17px}
        section.step-section{border-left:3px solid #8198ff;background:#12192b}
        section.tactic-section{border-color:#5269b9;background:#162040}
        section.tactic-section h2{color:#aebdff}
        p.clue{border-left:3px solid #8198ff;padding:8px 12px;background:#222a42;border-radius:0 9px 9px 0}
        ul,ol{margin:12px 0;padding-left:22px}
        li{padding:4px 0;font-size:15.5px}
        .math-block{margin:16px 0;padding:12px 8px;border-radius:12px;overflow-x:auto;text-align:center;background:#1b2231;color:#f6f7ff}
        mjx-container[display="true"]{overflow-x:auto;overflow-y:hidden;max-width:100%;margin:.5em 0!important;padding:4px 1px}
        hr{border:0;border-top:1px solid #303952;margin:20px 0}
        .empty{color:#aeb9d0;text-align:center;padding:18px}
        </style>
        <script>
        var bs=String.fromCharCode(92);
        window.MathJax={tex:{inlineMath:[['$','$'],[bs+'(',bs+')']],displayMath:[['$$','$$'],[bs+'[',bs+']']]},svg:{fontCache:'local'},options:{skipHtmlTags:['script','style','pre','code']},startup:{typeset:true}};
        function reportHeight(){
          if(!window.OptikHeight)return;
          var h=Math.ceil(Math.max(document.body.scrollHeight,document.documentElement.scrollHeight)+24);
          window.OptikHeight.setHeight(h);
        }
        window.addEventListener('DOMContentLoaded',function(){
          reportHeight();
          if(window.ResizeObserver)new ResizeObserver(reportHeight).observe(document.body);
          setTimeout(reportHeight,250);
          setTimeout(reportHeight,1200);
          setTimeout(reportHeight,3500);
        });
        </script>
        <script src="mathjax/tex-svg.js"></script>
        </head><body>
        """;

    private static final String FOOT = """
        </body></html>
        """;
}
