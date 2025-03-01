import React from "react";
import { useParams } from "react-router-dom";
import { getCausesByCategory } from "../../DataFetching/DataFetching";
import CauseCard from "../../components/CausesComponents/CauseCard";

const CausesByCategory = () => {
  const { categoryId } = useParams();
  const [causes, setCauses] = React.useState([]);
  const [loading, setLoading] = React.useState(false);
  const [page, setPage] = React.useState(1);
  const [paginationData, setPaginationData] = React.useState({
    totalPages: 0,
    totalElements: 0,
  });
  const [categoryInfo, setCategoryInfo] = React.useState({
    name: "",
    description: "",
  });

  React.useEffect(() => {
    setLoading(true);
    getCausesByCategory(categoryId)
      .then((response) => {
        const { content, totalPages, totalElements } = response.data.data;
        setCauses(content);
        setPaginationData({ totalPages, totalElements });

        // Set category info from the first cause's category data
        if (content && content[0]?.category) {
          setCategoryInfo({
            name: content[0].category.name,
            description: content[0].category.description,
          });
        }
      })
      .catch((error) => console.error(error))
      .finally(() => setLoading(false));
  }, [categoryId, page]);

  const handlePageChange = (pageNumber) => {
    setPage(pageNumber);
  };

  const CategoryStats = () => (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
      <div className="bg-gray-50 rounded-lg p-4">
        <div className="text-sm text-gray-500">Total Causes</div>
        <div className="text-2xl font-bold text-gray-900">
          {paginationData.totalElements}
        </div>
      </div>
      {causes[0] && (
        <>
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="text-sm text-gray-500">Active Campaigns</div>
            <div className="text-2xl font-bold text-gray-900">
              {causes.filter((cause) => cause.status === "ACTIVE").length}
            </div>
          </div>
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="text-sm text-gray-500">Total Donors</div>
            <div className="text-2xl font-bold text-gray-900">
              {causes.reduce((acc, cause) => acc + (cause.donorCount || 0), 0)}
            </div>
          </div>
        </>
      )}
    </div>
  );

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Category Header */}
      <div className="mb-8 text-center sm:text-left">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            {categoryInfo.name}
          </h1>
          <div className="h-1 w-20 bg-primary mx-auto sm:mx-0 mb-4"></div>
          <p className="text-gray-600 text-sm sm:text-base leading-relaxed">
            {categoryInfo.description}
          </p>
          <div className="mt-4 text-sm text-gray-500">
            {paginationData.totalElements}{" "}
            {paginationData.totalElements === 1 ? "cause" : "causes"} available
          </div>
        </div>
      </div>

      {/* Category Stats */}
      <CategoryStats />

      {/* Divider */}
      <div className="w-full h-px bg-gray-200 mb-8"></div>

      {/* Causes Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6 md:gap-8">
        {causes.map((cause) => (
          <div key={cause.id} className="w-full">
            <CauseCard
              causeId={cause?.id}
              causeFeaturedImageUrl={cause?.featuredImageUrl}
              causeTitle={cause?.title}
              causeDescription={cause?.description}
              causeCategory={cause?.category}
              causeCountry={cause?.country}
              causeGoalAmount={cause?.goalAmount}
              causeCurrentAmount={cause?.currentAmount}
              causeOrganization={cause?.organization}
              causeDonorCount={cause?.donorCount}
              causeViewCount={cause?.viewCount}
              causeEndDate={cause?.endDate}
              causeStatus={cause?.status}
            />
          </div>
        ))}
      </div>

      {/* Custom Pagination */}
      {paginationData.totalPages > 1 && (
        <div className="flex justify-center items-center space-x-2 mt-8">
          <button
            onClick={() => handlePageChange(page - 1)}
            disabled={page === 1}
            className={`px-4 py-2 rounded-lg ${
              page === 1
                ? "bg-gray-200 text-gray-500 cursor-not-allowed"
                : "bg-primary text-white hover:bg-primary/90"
            }`}>
            Previous
          </button>

          <div className="flex space-x-2">
            {[...Array(paginationData.totalPages)].map((_, index) => (
              <button
                key={index + 1}
                onClick={() => handlePageChange(index + 1)}
                className={`w-10 h-10 rounded-lg ${
                  page === index + 1
                    ? "bg-primary text-white"
                    : "bg-gray-200 hover:bg-gray-300"
                }`}>
                {index + 1}
              </button>
            ))}
          </div>

          <button
            onClick={() => handlePageChange(page + 1)}
            disabled={page === paginationData.totalPages}
            className={`px-4 py-2 rounded-lg ${
              page === paginationData.totalPages
                ? "bg-gray-200 text-gray-500 cursor-not-allowed"
                : "bg-primary text-white hover:bg-primary/90"
            }`}>
            Next
          </button>
        </div>
      )}

      {/* No Results Message */}
      {causes.length === 0 && !loading && (
        <div className="text-center py-12">
          <h3 className="text-xl font-semibold text-gray-700">
            No causes found in this category
          </h3>
          <p className="text-gray-500 mt-2">
            Please try selecting a different category
          </p>
        </div>
      )}
    </div>
  );
};

export default CausesByCategory;
