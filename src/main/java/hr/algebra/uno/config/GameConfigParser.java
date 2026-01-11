package hr.algebra.uno.config;

import hr.algebra.uno.exception.ConfigException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GameConfigParser {

    private static final String XML_PATH = "xml/game-config.xml";

    private GameConfigParser() {}

    public static GameConfig load() {
        try {
            Path xmlPath = Path.of(XML_PATH);

            if (!Files.exists(xmlPath)) {
                throw new Exception("Config file not found: " + xmlPath.toAbsolutePath());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());

            Document doc = builder.parse(xmlPath.toFile());
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            GameConfig config = new GameConfig();

            // RULES
            Element rules = (Element) root
                    .getElementsByTagName(ConfigTag.RULES.tag())
                    .item(0);

            config.setStartingCards(intValue(rules, ConfigTag.STARTING_CARDS));
            config.setMustCallUno(boolValue(rules, ConfigTag.MUST_CALL_UNO));
            config.setUnoPenaltyCards(intValue(rules, ConfigTag.UNO_PENALTY_CARDS));
            config.setAllowStacking(boolValue(rules, ConfigTag.ALLOW_STACKING));
            config.setDrawUntilPlayable(boolValue(rules, ConfigTag.DRAW_UNTIL_PLAYABLE));
            config.setInitialDirection(Direction.valueOf(text(rules, ConfigTag.INITIAL_DIRECTION)));

            // COMPUTER
            Element computer = (Element) root
                    .getElementsByTagName(ConfigTag.COMPUTER.tag())
                    .item(0);

            config.setComputerThinkingDelayMin(intValue(computer, ConfigTag.THINKING_DELAY_MIN));
            config.setComputerThinkingDelayMax(intValue(computer, ConfigTag.THINKING_DELAY_MAX));
            config.setComputerCallUnoProbability(doubleValue(computer, ConfigTag.CALL_UNO_PROBABILITY));

            // UI
            Element ui = (Element) root
                    .getElementsByTagName(ConfigTag.UI.tag())
                    .item(0);

            config.setEnableAnimations(boolValue(ui, ConfigTag.ENABLE_ANIMATIONS));
            config.setShowPlayableHints(boolValue(ui, ConfigTag.SHOW_PLAYABLE_HINTS));

            return config;

        } catch (Exception e) {
            throw new ConfigException("Failed to load game config XML", e);
        }
    }

    private static String text(Element parent, ConfigTag tag) {
        return parent.getElementsByTagName(tag.tag())
                .item(0)
                .getTextContent()
                .trim();
    }

    private static int intValue(Element parent, ConfigTag tag) {
        return Integer.parseInt(text(parent, tag));
    }

    private static boolean boolValue(Element parent, ConfigTag tag) {
        return Boolean.parseBoolean(text(parent, tag));
    }

    private static double doubleValue(Element parent, ConfigTag tag) {
        return Double.parseDouble(text(parent, tag));
    }
}
