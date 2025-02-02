import { useQuery } from "react-query";

import ProjectDetail from "./ProjectDetail";
import { useHomeClient } from "../../context/Client/HomeClientContext";

const Project = ({ type }) => {
  const { getPopularProjects, getImminentProjects, getUpToDateProjects } =
    useHomeClient();

  const { data: projectList = [] } = useQuery(
    type === "popular"
      ? "popularProjects"
      : type === "imminent"
      ? "imminentProjects"
      : "upToDateProjects",
    () =>
      type === "popular"
        ? getPopularProjects()
        : type === "imminent"
        ? getImminentProjects()
        : getUpToDateProjects()
  );

  return (
    <div className="project">
      <h1>
        {type === "popular"
          ? "실시간 인기 프로젝트"
          : type === "imminent"
          ? "마감 임박 프로젝트"
          : "최근 올라온 프로젝트"}
      </h1>
      <ProjectDetail type={type} projectList={projectList} />
    </div>
  );
};

export default Project;
