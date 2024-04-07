/**
 * Java module which allows to interact with <a href="http://wiki.ros.org/Services">ROS1 (Robot
 * Operating System) Services</a>.
 *
 * <p>For usage examples see <a href="http://pinoweb.freetzi.com/jrosservices">Documentation</a>
 *
 * @see <a href="https://github.com/pinorobotics/jros1services">GitHub repository</a>
 * @see <a
 *     href="https://github.com/pinorobotics/jros1services/blob/main/jros1services/release/CHANGELOG.md">Releases</a>
 * @see <a href="http://wiki.ros.org/Services">ROS1 Services</a>
 * @author lambdaprime intid@protonmail.com
 */
module jros1services {
    requires transitive jros1messages;
    requires transitive jros1client;
    requires transitive jrosservices;
    requires jrosclient;
    requires id.xfunction;
    requires jrosmessages;

    exports pinorobotics.jros1services;
}
