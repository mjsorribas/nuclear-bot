package nuclearbot.builtin.osu.commands;

import nuclearbot.builtin.osu.OsuFetcher;
import nuclearbot.builtin.osu.OsuPlugin;
import nuclearbot.builtin.osu.data.DataBeatmap;
import nuclearbot.client.ChatClient;
import nuclearbot.client.Command;
import nuclearbot.plugin.CommandExecutor;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Copyright (C) 2017 NuclearCoder
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Command "req" to request a beatmap (takes beatmaps, beatmap sets, and external URLs).<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class CommandRequest implements CommandExecutor {

    private static final Pattern REGEX_BEATMAP_URL = Pattern.compile("^https?://osu\\.ppy\\.sh/([bs])/([0-9]+)(&.*)?$");

    private static final String MSG_REQUEST_NOT_FOUND = "No beatmap found.";
    private static final String MSG_REQUEST_OTHER = "Request: %s";
    private static final String MSG_REQUEST_BEATMAPSET = "Request: %s - %s (%d diffs)";
    private static final String MSG_REQUEST_BEATMAP = "Request: %s - %s [%s] (creator %s) \u00A6 BPM %d \u00A6 AR %.1f \u00A6 %.2f stars";

    private static final String PRIVMSG_REQUEST_OTHER = "Request from %s: %s";
    private static final String PRIVMSG_REQUEST_BEATMAPSET = "Request from %s: [%s %s - %s] (%d diffs)";
    private static final String PRIVMSG_REQUEST_BEATMAP = "Request from %s: [%s %s - %s] \u00A6 BPM %d \u00A6 AR %.1f \u00A6 %.2f stars";
    private static final String PRIVMSG_REQUEST_MESSAGE = "(%s)";

    private final OsuPlugin m_osu;
    private final OsuFetcher m_fetcher;

    public CommandRequest(final OsuPlugin osu) {
        m_osu = osu;
        m_fetcher = osu.getFetcher();
    }

    @Override
    public boolean onCommand(final ChatClient client, final String username, final Command command, final String label, final String[] args)
            throws IOException {
        if (args.length < 2) {
            return false;
        } else {
            final String beatmapUrl = args[1].toLowerCase();
            final Matcher matcher = REGEX_BEATMAP_URL.matcher(beatmapUrl);
            if (matcher.matches()) {
                final boolean isBeatmapset = matcher.group(1).equalsIgnoreCase("s");
                final int id = Integer.parseInt(matcher.group(2));
                if (isBeatmapset) {
                    final DataBeatmap[] beatmapset = m_fetcher.getBeatmapset(id);
                    if (beatmapset == null || beatmapset.length == 0) {
                        client.sendMessage(MSG_REQUEST_NOT_FOUND);
                        return true;
                    } else {
                        final String artist = beatmapset[0].getArtist();
                        final String title = beatmapset[0].getTitle();
                        client.sendMessage(String.format(MSG_REQUEST_BEATMAPSET, artist, title, beatmapset.length));
                        m_osu.sendPrivateMessage(String.format(PRIVMSG_REQUEST_BEATMAPSET, username, beatmapUrl, artist, title, beatmapset.length));
                    }
                } else {
                    final DataBeatmap beatmap = m_fetcher.getBeatmap(id);
                    if (beatmap == null) {
                        client.sendMessage(MSG_REQUEST_NOT_FOUND);
                        return true;
                    } else {
                        final String artist = beatmap.getArtist();
                        final String title = beatmap.getTitle();
                        final String version = beatmap.getVersion();
                        final String creator = beatmap.getCreator();
                        final int bpm = beatmap.getBPM();
                        final float diffAR = beatmap.getDiffAR();
                        final float difficultyRating = beatmap.getDifficultyRating();
                        client.sendMessage(String.format(MSG_REQUEST_BEATMAP, artist, title, version, creator, bpm, diffAR, difficultyRating));
                        m_osu.sendPrivateMessage(
                                String.format(PRIVMSG_REQUEST_BEATMAP, username, beatmapUrl, artist, title, bpm, diffAR, difficultyRating));
                    }
                }
            } else {
                client.sendMessage(String.format(MSG_REQUEST_OTHER, beatmapUrl));
                m_osu.sendPrivateMessage(String.format(PRIVMSG_REQUEST_OTHER, username, beatmapUrl));
            }
            if (args.length > 2) {
                m_osu.sendPrivateMessage(String.format(PRIVMSG_REQUEST_MESSAGE, String.join(" ", Arrays.copyOfRange(args, 2, args.length))));
            }
            return true;
        }
    }

}
